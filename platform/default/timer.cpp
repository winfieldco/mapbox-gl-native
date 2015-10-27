#include <mbgl/util/timer.hpp>

#include <mbgl/util/run_loop.hpp>
#include <mbgl/util/uv_detail.hpp>

namespace mbgl {
namespace util {

class Timer::Impl {
public:
    Impl()
        : timer(RunLoop::getLoop()) {
        timer.unref();
    }

    uv::timer timer;
};

Timer::Timer()
    : impl(std::make_unique<Impl>()) {
}

Timer::~Timer() {
}

void Timer::start(uint64_t timeout, std::function<void()>&& cb) {
    impl->timer.start(timeout, 0, std::move(cb));
}

void Timer::stop() {
    impl->timer.stop();
}

}
}
