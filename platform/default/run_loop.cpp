#include <mbgl/util/run_loop.hpp>
#include <mbgl/util/async_task.hpp>
#include <mbgl/util/uv.hpp>

#include <functional>
#include <unordered_map>

namespace mbgl {
namespace util {

static __thread RunLoop *current;

struct Watch {
    static void onEvent(uv_poll_t* poll, int, int event) {
        auto watch = reinterpret_cast<Watch*>(poll->data);

        RunLoop::Event watchEvent = RunLoop::Event::None;
        switch (event) {
        case UV_READABLE:
            watchEvent = RunLoop::Event::Read;
            break;
        case UV_WRITABLE:
            watchEvent = RunLoop::Event::Write;
            break;
        case UV_READABLE | UV_WRITABLE:
            watchEvent = RunLoop::Event::ReadWrite;
            break;
        }

        watch->eventCallback(watch->fd, watchEvent);
    };

    static void onClose(uv_handle_t *poll) {
        auto watch = reinterpret_cast<Watch*>(poll->data);
        watch->closeCallback();
    };

    uv_poll_t poll;
    int fd;

    std::function<void(int, RunLoop::Event)> eventCallback;
    std::function<void()> closeCallback;
};

RunLoop* RunLoop::Get() {
    return current;
}

class RunLoop::Impl {
public:
    Impl() = default;

    uv_loop_t *loop;
    RunLoop::Type type;
    std::unique_ptr<AsyncTask> async;

    std::unordered_map<int, std::unique_ptr<Watch>> watchPoll;
};

RunLoop::RunLoop(Type type) : impl(std::make_unique<Impl>()) {
    switch (type) {
    case Type::New:
#if UV_VERSION_MAJOR == 0 && UV_VERSION_MINOR <= 10
        impl->loop = uv_loop_new();
        if (impl->loop == nullptr) {
#else
        impl->loop = new uv_loop_t;
        if (uv_loop_init(impl->loop) != 0) {
#endif
            throw std::runtime_error("Failed to initialize loop.");
        }
        break;
    case Type::Default:
        impl->loop = uv_default_loop();
        break;
    }

    impl->type = type;

    current = this;
    impl->async = std::make_unique<AsyncTask>(std::bind(&RunLoop::process, this));
}

RunLoop::~RunLoop() {
    current = nullptr;

    if (impl->type == Type::Default) {
        return;
    }

    // Run the loop again to ensure that async
    // close callbacks have been called. Not needed
    // for the default main loop because it is only
    // closed when the application exits.
    impl->async.reset();
    runOnce();

#if UV_VERSION_MAJOR == 0 && UV_VERSION_MINOR <= 10
    uv_loop_delete(impl->loop);
#else
    if (uv_loop_close(impl->loop) == UV_EBUSY) {
        throw std::runtime_error("Failed to close loop.");
    }
    delete impl->loop;
#endif
}

LOOP_HANDLE RunLoop::getLoopHandle() {
    return current->impl->loop;
}

void RunLoop::push(std::shared_ptr<WorkTask> task) {
    withMutex([&] { queue.push(task); });
    impl->async->send();
}

void RunLoop::run() {
    MBGL_VERIFY_THREAD(tid);

    uv_run(impl->loop, UV_RUN_DEFAULT);
}

void RunLoop::runOnce() {
    MBGL_VERIFY_THREAD(tid);

    uv_run(impl->loop, UV_RUN_ONCE);
}

void RunLoop::stop() {
    invoke([&] { impl->async->unref(); });
}

void RunLoop::addWatch(int fd, Event event, std::function<void(int, Event)>&& callback) {
    MBGL_VERIFY_THREAD(tid);

    Watch *watch = nullptr;
    auto watchPollIter = impl->watchPoll.find(fd);

    if (watchPollIter == impl->watchPoll.end()) {
        std::unique_ptr<Watch> watchPtr = std::make_unique<Watch>();

        watch = watchPtr.get();
        impl->watchPoll[fd] = std::move(watchPtr);

        if (uv_poll_init(impl->loop, &watch->poll, fd)) {
            throw std::runtime_error("Failed to init poll on file descriptor.");
        }
    } else {
        watch = watchPollIter->second.get();
    }

    watch->poll.data = watch;
    watch->fd = fd;
    watch->eventCallback = std::move(callback);

    int pollEvent = 0;
    switch (event) {
    case Event::Read:
        pollEvent = UV_READABLE;
        break;
    case Event::Write:
        pollEvent = UV_WRITABLE;
        break;
    case Event::ReadWrite:
        pollEvent = UV_READABLE | UV_WRITABLE;
        break;
    default:
        throw std::runtime_error("Unhandled event.");
    }

    if (uv_poll_start(&watch->poll, pollEvent, &Watch::onEvent)) {
        throw std::runtime_error("Failed to start poll on file descriptor.");
    }
}

void RunLoop::removeWatch(int fd) {
    MBGL_VERIFY_THREAD(tid);

    auto watchPollIter = impl->watchPoll.find(fd);
    if (watchPollIter == impl->watchPoll.end()) {
        return;
    }

    Watch* watch = watchPollIter->second.release();
    impl->watchPoll.erase(watchPollIter);

    watch->closeCallback = [watch] {
        delete watch;
    };

    if (uv_poll_stop(&watch->poll)) {
        throw std::runtime_error("Failed to stop poll on file descriptor.");
    }

    uv_close(reinterpret_cast<uv_handle_t*>(&watch->poll), &Watch::onClose);
}

}
}
