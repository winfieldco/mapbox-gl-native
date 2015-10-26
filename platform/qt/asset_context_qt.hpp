#ifndef PLATFORM_QT_ASSET_CONTEXT_QT
#define PLATFORM_QT_ASSET_CONTEXT_QT

#include <mbgl/storage/asset_context_base.hpp>
#include <mbgl/storage/resource.hpp>

#include <memory>

namespace mbgl {

class AssetRequestBase;
class HTTPQtContext;

class AssetQtContext : public AssetContextBase
{
public:
    AssetQtContext();
    virtual ~AssetQtContext() = default;

    // AssetContextBase implementation.
    RequestBase* createRequest(const Resource&,
                               RequestBase::Callback,
                               const std::string& assetRoot) final;

private:
    std::unique_ptr<HTTPQtContext> context;
};

} // namespace mbgl

#endif // PLATFORM_QT_ASSET_CONTEXT_QT
