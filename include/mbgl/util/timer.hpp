#ifndef MBGL_UTIL_TIMER
#define MBGL_UTIL_TIMER

#include <mbgl/util/noncopyable.hpp>

#include <memory>
#include <functional>

namespace mbgl {
namespace util {

class Timer : private util::noncopyable {
public:
    Timer();
    ~Timer();

    void start(uint64_t timeout, std::function<void()>&&);
    void stop();

private:
    class Impl;
    std::unique_ptr<Impl> impl;
};

}
}

#endif
