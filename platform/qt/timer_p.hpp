#include <mbgl/util/timer.hpp>

#include <QObject>
#include <QTimer>

namespace mbgl {
namespace util {

class Timer::Impl : public QObject {
    Q_OBJECT

public:
    Impl();

    void start(uint64_t timeout, std::function<void()>&& cb);
    void stop();

public slots:
    void timerFired();

private:
    QTimer timer;
    std::function<void()> callback;
};

}
}
