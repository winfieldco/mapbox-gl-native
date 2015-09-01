#include "storage.hpp"

#include <mbgl/storage/default_file_source.hpp>
#include <mbgl/storage/network_status.hpp>
#include <mbgl/util/run_loop.hpp>

#include <cmath>

TEST_F(Storage, HTTPCancel) {
    SCOPED_TEST(HTTPCancel)

    using namespace mbgl;

    DefaultFileSource fs(nullptr);
    util::RunLoop loop;

    const Resource resource { Resource::Unknown, "http://127.0.0.1:3000/test" };

    std::unique_ptr<FileRequest> req2 = fs.request(resource, [&](Response) {
        ADD_FAILURE() << "Callback should not be called";
    });
    std::unique_ptr<FileRequest> req = fs.request(resource, [&](Response res) {
        req.reset();
        EXPECT_EQ(nullptr, res.error);
        EXPECT_EQ(false, res.stale);
        ASSERT_TRUE(res.data.get());
        EXPECT_EQ("Hello World!", *res.data);
        EXPECT_EQ(0, res.expires);
        EXPECT_EQ(0, res.modified);
        EXPECT_EQ("", res.etag);
        loop.stop();
        HTTPCancel.finish();
    });
    req2.reset();

    loop.run();
}
