#include "../fixtures/util.hpp"
#include "../fixtures/fixture_log_observer.hpp"

#include <mbgl/map/map.hpp>
#include <mbgl/map/still_image.hpp>
#include <mbgl/platform/default/headless_display.hpp>
#include <mbgl/platform/default/headless_view.hpp>
#include <mbgl/storage/default_file_source.hpp>
#include <mbgl/util/exception.hpp>
#include <mbgl/util/run_loop.hpp>

#include <future>

using namespace mbgl;

TEST(API, RenderWithoutCallback) {
    FixtureLogObserver* log = new FixtureLogObserver();
    Log::setObserver(std::unique_ptr<Log::Observer>(log));

    util::RunLoop loop;

    auto display = std::make_shared<mbgl::HeadlessDisplay>();
    HeadlessView view(display, 1);
    view.resize(128, 512);
    DefaultFileSource fileSource(nullptr);

    std::unique_ptr<Map> map = std::make_unique<Map>(view, fileSource, MapMode::Still);
    map->renderStill(nullptr);

    // Force Map thread to join.
    map.reset();

    const FixtureLogObserver::LogMessage logMessage {
        EventSeverity::Error,
        Event::General,
        int64_t(-1),
        "StillImageCallback not set",
    };

    EXPECT_EQ(log->count(logMessage), 1u);
}

TEST(API, RenderWithoutStyle) {
    util::RunLoop loop;

    auto display = std::make_shared<mbgl::HeadlessDisplay>();
    HeadlessView view(display, 1);
    view.resize(128, 512);
    DefaultFileSource fileSource(nullptr);

    Map map(view, fileSource, MapMode::Still);

    std::exception_ptr error;
    map.renderStill([&](std::exception_ptr error_, std::unique_ptr<const StillImage>) {
        error = error_;
        loop.stop();
    });

    loop.run();

    try {
        std::rethrow_exception(error);
    } catch (const util::MisuseException& ex) {
        EXPECT_EQ(std::string(ex.what()), "Map doesn't have a style");
    } catch (const std::exception&) {
        EXPECT_TRUE(false) << "Unhandled exception.";
    }
}
