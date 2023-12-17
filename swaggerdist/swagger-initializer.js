window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">
  var queryStr = window.location.search;
  var paramPairs = queryStr.substr(1).split('&');
  var params = {};
  for (var i = 0; i < paramPairs.length; i++) {
      var parts = paramPairs[i].split('=');
      params[parts[0]] = parts[1];
  }

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: params.spec,
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });

  //</editor-fold>
};
