#include "asset_context_qt.hpp"

#include "http_context_qt.hpp"
#include "http_request_qt.hpp"

#include <QByteArray>
#include <QUrl>

namespace mbgl {

AssetQtContext::AssetQtContext() : context(std::make_unique<HTTPQtContext>())
{
    // HTTPQtContext will use QNetworkAccessManager behind
    // the scenes and it can handle file:// protocol the same
    // way as it deals with http://.
}

RequestBase* AssetQtContext::createRequest(const Resource& resource,
    RequestBase::Callback callback, const std::string& assetRoot)
{
    std::string urlStr = resource.url;
    QUrl url = QUrl::fromPercentEncoding(QByteArray(urlStr.data(), urlStr.size()));

    if (url.scheme() == "asset") {
        QByteArray root(assetRoot.data(), assetRoot.size());
        url.setUrl("file://" + QString(root) + "/" + url.host() + url.path());

        QByteArray fileScheme = url.toEncoded();
        urlStr = std::string(fileScheme.constData(), fileScheme.size());
    }

    return new HTTPQtRequest(context.get(), Resource{resource.kind, urlStr}, callback, nullptr);
}

std::unique_ptr<AssetContextBase> AssetContextBase::createContext()
{
    return std::make_unique<AssetQtContext>();
}

} // namespace mbgl
