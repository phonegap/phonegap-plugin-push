# Cordova Hook Scripts for ADM Support

Add the following hooks to your cordova project:

    <platform name="android">
      <hook src="scripts/copy_adm_key.js" type="after_prepare" />
      <hook src="scripts/add_provided_adm_jar.js" type="after_prepare" />
      <hook src="scripts/add_amazon_ns_to_manifest.js" type="after_prepare" />
    </platform>

Adapt the location of your api key in `copy_adm_key.js`.
