#include <mbgl/util/run_loop.hpp>
#include <mbgl/util/uv_detail.hpp>

namespace mbgl {
namespace util {

static uv::tls<RunLoop> current;

class RunLoop::Impl {
public:
    Impl(std::function<void ()> process) : async(loop.get(), process) {}

    uv::loop loop;
    uv::async async;
};

RunLoop::RunLoop() : impl(std::make_unique<Impl>(std::bind(&RunLoop::process, this))) {
    current.set(this);
}

RunLoop::~RunLoop() {
    current.set(nullptr);
}

RunLoop* RunLoop::Get() {
    return current.get();
}

LOOP_HANDLE RunLoop::getLoopHandle() {
    return current.get()->impl->loop.get();
}

void RunLoop::push(std::shared_ptr<WorkTask> task) {
    withMutex([&] { queue.push(task); });
    impl->async.send();
}

void RunLoop::run() {
    impl->loop.run();
}

void RunLoop::stop() {
    invoke([&] { impl->async.unref(); });
}

}
}
