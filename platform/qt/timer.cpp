#include "timer_p.hpp"

namespace mbgl {
namespace util {

Timer::Impl::Impl() {
    timer.setSingleShot(true);
    connect(&timer, SIGNAL(timeout()), this, SLOT(timerFired()));
}

void Timer::Impl::start(uint64_t timeout, std::function<void()>&& cb) {
    callback = std::move(cb);
    timer.start(timeout);
}

void Timer::Impl::stop() {
    timer.stop();
}

void Timer::Impl::timerFired() {
    callback();
}

Timer::Timer()
    : impl(std::make_unique<Impl>()) {
}

Timer::~Timer() {
}

void Timer::start(uint64_t timeout, std::function<void()>&& cb) {
    impl->start(timeout, std::move(cb));
}

void Timer::stop() {
    impl->stop();
}

}
}
