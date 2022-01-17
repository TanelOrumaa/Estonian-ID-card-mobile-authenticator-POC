// vue.config.js
module.exports = {
    chainWebpack: config => {
        config.module.rule('vue').uses.delete('cache-loader');
        config.module.rule('js').uses.delete('cache-loader');
        config.module.rule('ts').uses.delete('cache-loader');
        config.module.rule('tsx').uses.delete('cache-loader');
    },
    // https.//cli.vuejs.org/config/#devserver-proxy
    devServer: {
        port: 3000,
        proxy: {
            "/auth": {
                target: "http://localhost:8080",
                ws: true,
                changeOrigin: true
            }
        }
    },
}