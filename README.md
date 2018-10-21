# phonegap-plugin-push [![Build Status](https://travis-ci.org/phonegap/phonegap-plugin-push.svg)](https://travis-ci.org/phonegap/phonegap-plugin-push)

> Register and receive push notifications

# Warning

The links below take you to the version 2.x documentation which includes a
number of breaking API changes from version 1.x, mostly the move from GCM to
FCM. If you are using version 1.x please reference the docs in the
[v1.x branch](https://github.com/phonegap/phonegap-plugin-push/tree/v1.x).

# What is this?

This plugin offers support to receive and handle native push notifications with
a **single unified API**.

This does not mean you will be able to send a single push message and have it
arrive on devices running different operating systems. By default Android uses
FCM and iOS uses APNS and their payloads are significantly different. Even if
you are using FCM for both Android and iOS there are differences in the payload
required for the plugin to work correctly. For Android **always** put your push
payload in the `data` section of the push notification. For more information on
why that is the case read
[Notification vs Data Payload](https://github.com/phonegap/phonegap-plugin-push/blob/master/docs/PAYLOAD.md#notification-vs-data-payloads).
For iOS follow the regular
[FCM documentation](https://firebase.google.com/docs/cloud-messaging/http-server-ref).

This plugin does not provide a way to determine which platform you are running
on. The best way to do that is use the `device.platform` property provided by
[cordova-plugin-device](https://github.com/apache/cordova-plugin-device).

Starting with version `2.0.0`, this plugin will support `CocoaPods` installation
of the `Firebase Cloud Messaging` library. More details are available in the
[Installation](docs/INSTALLATION.md#cocoapods) documentation.

* [Reporting Issues](docs/ISSUES.md)
* [Installation](docs/INSTALLATION.md)
* [API reference](docs/API.md)
* [Typescript support](docs/TYPESCRIPT.md)
* [Examples](docs/EXAMPLES.md)
* [Platform support](docs/PLATFORM_SUPPORT.md)
* [Cloud build support (PG Build, IntelXDK)](docs/PHONEGAP_BUILD.md)
* [Push notification payload details](docs/PAYLOAD.md)
* [Contributing](.github/CONTRIBUTING.md)
* [License (MIT)](MIT-LICENSE)

# Do you like tutorial? You get tutorial!

* [PhoneGap Day US Push Workshop 2016 (using node-gcm)](http://macdonst.github.io/push-workshop/)

# Thanks to all our contributors

[<img alt="10ko" src="https://avatars1.githubusercontent.com/u/2706375?v=4&s=117" width="117">](https://github.com/10ko)[<img alt="TVolly" src="https://avatars3.githubusercontent.com/u/20628284?v=4&s=117" width="117">](https://github.com/TVolly)[<img alt="waptaxi" src="https://avatars2.githubusercontent.com/u/6575569?v=4&s=117" width="117">](https://github.com/waptaxi)[<img alt="viktormuller" src="https://avatars1.githubusercontent.com/u/8171222?v=4&s=117" width="117">](https://github.com/viktormuller)[<img alt="devgeeks" src="https://avatars0.githubusercontent.com/u/554999?v=4&s=117" width="117">](https://github.com/devgeeks)[<img alt="rastafan" src="https://avatars3.githubusercontent.com/u/7632849?v=4&s=117" width="117">](https://github.com/rastafan)

[<img alt="mdoelker" src="https://avatars1.githubusercontent.com/u/2145319?v=4&s=117" width="117">](https://github.com/mdoelker)[<img alt="markeeftb" src="https://avatars3.githubusercontent.com/u/3172570?v=4&s=117" width="117">](https://github.com/markeeftb)[<img alt="malwatte" src="https://avatars2.githubusercontent.com/u/2534778?v=4&s=117" width="117">](https://github.com/malwatte)[<img alt="madebycm" src="https://avatars3.githubusercontent.com/u/5081823?v=4&s=117" width="117">](https://github.com/madebycm)[<img alt="kelvinhokk" src="https://avatars0.githubusercontent.com/u/7766953?v=4&s=117" width="117">](https://github.com/kelvinhokk)[<img alt="keab42" src="https://avatars0.githubusercontent.com/u/1453371?v=4&s=117" width="117">](https://github.com/keab42)

[<img alt="jomarocas" src="https://avatars3.githubusercontent.com/u/5194064?v=4&s=117" width="117">](https://github.com/jomarocas)[<img alt="giuseppelt" src="https://avatars2.githubusercontent.com/u/7147291?v=4&s=117" width="117">](https://github.com/giuseppelt)[<img alt="ericb" src="https://avatars3.githubusercontent.com/u/19638?v=4&s=117" width="117">](https://github.com/ericb)[<img alt="eKazim" src="https://avatars3.githubusercontent.com/u/21195186?v=4&s=117" width="117">](https://github.com/eKazim)[<img alt="clementcontet" src="https://avatars1.githubusercontent.com/u/7261426?v=4&s=117" width="117">](https://github.com/clementcontet)[<img alt="yaswanthsvist" src="https://avatars0.githubusercontent.com/u/3897387?v=4&s=117" width="117">](https://github.com/yaswanthsvist)

[<img alt="Vabs28" src="https://avatars1.githubusercontent.com/u/8149036?v=4&s=117" width="117">](https://github.com/Vabs28)[<img alt="TillaTheHun0" src="https://avatars0.githubusercontent.com/u/8246360?v=4&s=117" width="117">](https://github.com/TillaTheHun0)[<img alt="tomasvarg" src="https://avatars0.githubusercontent.com/u/16196322?v=4&s=117" width="117">](https://github.com/tomasvarg)[<img alt="tobmaster" src="https://avatars1.githubusercontent.com/u/203823?v=4&s=117" width="117">](https://github.com/tobmaster)[<img alt="ThiagoBueno" src="https://avatars1.githubusercontent.com/u/3334244?v=4&s=117" width="117">](https://github.com/ThiagoBueno)[<img alt="szh" src="https://avatars2.githubusercontent.com/u/546965?v=4&s=117" width="117">](https://github.com/szh)

[<img alt="SharUpOff" src="https://avatars3.githubusercontent.com/u/26085497?v=4&s=117" width="117">](https://github.com/SharUpOff)[<img alt="smorstabilini" src="https://avatars0.githubusercontent.com/u/551983?v=4&s=117" width="117">](https://github.com/smorstabilini)[<img alt="fesor" src="https://avatars2.githubusercontent.com/u/172247?v=4&s=117" width="117">](https://github.com/fesor)[<img alt="GreyDekart" src="https://avatars2.githubusercontent.com/u/1793548?v=4&s=117" width="117">](https://github.com/GreyDekart)[<img alt="sebastiansier" src="https://avatars1.githubusercontent.com/u/3733454?v=4&s=117" width="117">](https://github.com/sebastiansier)[<img alt="olastor" src="https://avatars0.githubusercontent.com/u/7479641?v=4&s=117" width="117">](https://github.com/olastor)

[<img alt="tanansatpal" src="https://avatars2.githubusercontent.com/u/20103208?v=4&s=117" width="117">](https://github.com/tanansatpal)[<img alt="SandroGrzicic" src="https://avatars1.githubusercontent.com/u/200247?v=4&s=117" width="117">](https://github.com/SandroGrzicic)[<img alt="xorxor" src="https://avatars1.githubusercontent.com/u/1525525?v=4&s=117" width="117">](https://github.com/xorxor)[<img alt="rubenstolk" src="https://avatars2.githubusercontent.com/u/692644?v=4&s=117" width="117">](https://github.com/rubenstolk)[<img alt="roel-sluper" src="https://avatars0.githubusercontent.com/u/1556404?v=4&s=117" width="117">](https://github.com/roel-sluper)[<img alt="pataar" src="https://avatars3.githubusercontent.com/u/3403851?v=4&s=117" width="117">](https://github.com/pataar)

[<img alt="peteonrails" src="https://avatars1.githubusercontent.com/u/9593?v=4&s=117" width="117">](https://github.com/peteonrails)[<img alt="pjalbuquerque" src="https://avatars3.githubusercontent.com/u/4201558?v=4&s=117" width="117">](https://github.com/pjalbuquerque)[<img alt="NitroGhost" src="https://avatars0.githubusercontent.com/u/3674467?v=4&s=117" width="117">](https://github.com/NitroGhost)[<img alt="matrosov-nikita" src="https://avatars3.githubusercontent.com/u/12752587?v=4&s=117" width="117">](https://github.com/matrosov-nikita)[<img alt="Mikejo5000" src="https://avatars1.githubusercontent.com/u/11948536?v=4&s=117" width="117">](https://github.com/Mikejo5000)[<img alt="michellarcari" src="https://avatars2.githubusercontent.com/u/6429722?v=4&s=117" width="117">](https://github.com/michellarcari)

[<img alt="adamschachne" src="https://avatars2.githubusercontent.com/u/13020251?v=4&s=117" width="117">](https://github.com/adamschachne)[<img alt="alharding" src="https://avatars1.githubusercontent.com/u/926204?v=4&s=117" width="117">](https://github.com/alharding)[<img alt="albertleao" src="https://avatars0.githubusercontent.com/u/1950338?v=4&s=117" width="117">](https://github.com/albertleao)[<img alt="gotev" src="https://avatars0.githubusercontent.com/u/16792495?v=4&s=117" width="117">](https://github.com/gotev)[<img alt="Alex-Sessler" src="https://avatars0.githubusercontent.com/u/5779673?v=4&s=117" width="117">](https://github.com/Alex-Sessler)[<img alt="ben-8409" src="https://avatars1.githubusercontent.com/u/305724?v=4&s=117" width="117">](https://github.com/ben-8409)

[<img alt="bmwertman" src="https://avatars3.githubusercontent.com/u/2573903?v=4&s=117" width="117">](https://github.com/bmwertman)[<img alt="bmatto" src="https://avatars1.githubusercontent.com/u/1044422?v=4&s=117" width="117">](https://github.com/bmatto)[<img alt="countcain" src="https://avatars0.githubusercontent.com/u/1751150?v=4&s=117" width="117">](https://github.com/countcain)[<img alt="CookieCookson" src="https://avatars3.githubusercontent.com/u/3473396?v=4&s=117" width="117">](https://github.com/CookieCookson)[<img alt="cdorner" src="https://avatars3.githubusercontent.com/u/917733?v=4&s=117" width="117">](https://github.com/cdorner)[<img alt="colene" src="https://avatars3.githubusercontent.com/u/1613781?v=4&s=117" width="117">](https://github.com/colene)

[<img alt="cfsnyder" src="https://avatars3.githubusercontent.com/u/3925941?v=4&s=117" width="117">](https://github.com/cfsnyder)[<img alt="cmalard" src="https://avatars0.githubusercontent.com/u/1692136?v=4&s=117" width="117">](https://github.com/cmalard)[<img alt="dansumption" src="https://avatars2.githubusercontent.com/u/174105?v=4&s=117" width="117">](https://github.com/dansumption)[<img alt="dannywillems" src="https://avatars2.githubusercontent.com/u/6018454?v=4&s=117" width="117">](https://github.com/dannywillems)[<img alt="DrMoriarty" src="https://avatars1.githubusercontent.com/u/1177068?v=4&s=117" width="117">](https://github.com/DrMoriarty)[<img alt="eladmoshe" src="https://avatars1.githubusercontent.com/u/1702227?v=4&s=117" width="117">](https://github.com/eladmoshe)

[<img alt="mlabarca" src="https://avatars2.githubusercontent.com/u/4587965?v=4&s=117" width="117">](https://github.com/mlabarca)[<img alt="bromeostasis" src="https://avatars3.githubusercontent.com/u/3764641?v=4&s=117" width="117">](https://github.com/bromeostasis)[<img alt="filmaj" src="https://avatars0.githubusercontent.com/u/52645?v=4&s=117" width="117">](https://github.com/filmaj)[<img alt="geo242" src="https://avatars3.githubusercontent.com/u/7529238?v=4&s=117" width="117">](https://github.com/geo242)[<img alt="gbenvenuti" src="https://avatars3.githubusercontent.com/u/331314?v=4&s=117" width="117">](https://github.com/gbenvenuti)[<img alt="polyn0m" src="https://avatars2.githubusercontent.com/u/1258130?v=4&s=117" width="117">](https://github.com/polyn0m)

[<img alt="jacquesdev" src="https://avatars1.githubusercontent.com/u/7842197?v=4&s=117" width="117">](https://github.com/jacquesdev)[<img alt="janpio" src="https://avatars0.githubusercontent.com/u/183673?v=4&s=117" width="117">](https://github.com/janpio)[<img alt="jakari" src="https://avatars2.githubusercontent.com/u/2283862?v=4&s=117" width="117">](https://github.com/jakari)[<img alt="purplecabbage" src="https://avatars3.githubusercontent.com/u/46134?v=4&s=117" width="117">](https://github.com/purplecabbage)[<img alt="theaccordance" src="https://avatars3.githubusercontent.com/u/1813001?v=4&s=117" width="117">](https://github.com/theaccordance)[<img alt="jonas-m-" src="https://avatars3.githubusercontent.com/u/1147572?v=4&s=117" width="117">](https://github.com/jonas-m-)

[<img alt="Chuckytuh" src="https://avatars3.githubusercontent.com/u/1127199?v=4&s=117" width="117">](https://github.com/Chuckytuh)[<img alt="leonardobazico" src="https://avatars1.githubusercontent.com/u/5280179?v=4&s=117" width="117">](https://github.com/leonardobazico)[<img alt="loslislo-lshift" src="https://avatars0.githubusercontent.com/u/17316151?v=4&s=117" width="117">](https://github.com/loslislo-lshift)[<img alt="luka5" src="https://avatars2.githubusercontent.com/u/1176296?v=4&s=117" width="117">](https://github.com/luka5)[<img alt="mac89" src="https://avatars3.githubusercontent.com/u/2988607?v=4&s=117" width="117">](https://github.com/mac89)[<img alt="markokeeffe" src="https://avatars2.githubusercontent.com/u/1211393?v=4&s=117" width="117">](https://github.com/markokeeffe)

[<img alt="mbektchiev" src="https://avatars1.githubusercontent.com/u/5744783?v=4&s=117" width="117">](https://github.com/mbektchiev)[<img alt="goya" src="https://avatars1.githubusercontent.com/u/208774?v=4&s=117" width="117">](https://github.com/goya)[<img alt="slorber" src="https://avatars0.githubusercontent.com/u/749374?v=4&s=117" width="117">](https://github.com/slorber)[<img alt="daserge" src="https://avatars1.githubusercontent.com/u/4272078?v=4&s=117" width="117">](https://github.com/daserge)[<img alt="smdvdsn" src="https://avatars2.githubusercontent.com/u/507093?v=4&s=117" width="117">](https://github.com/smdvdsn)[<img alt="ryanluker" src="https://avatars2.githubusercontent.com/u/1335972?v=4&s=117" width="117">](https://github.com/ryanluker)

[<img alt="russellbeattie" src="https://avatars1.githubusercontent.com/u/166835?v=4&s=117" width="117">](https://github.com/russellbeattie)[<img alt="rjmunro" src="https://avatars0.githubusercontent.com/u/108641?v=4&s=117" width="117">](https://github.com/rjmunro)[<img alt="hanicker" src="https://avatars3.githubusercontent.com/u/510258?v=4&s=117" width="117">](https://github.com/hanicker)[<img alt="mwbrooks" src="https://avatars1.githubusercontent.com/u/21328?v=4&s=117" width="117">](https://github.com/mwbrooks)[<img alt="LightZam" src="https://avatars3.githubusercontent.com/u/5077142?v=4&s=117" width="117">](https://github.com/LightZam)[<img alt="laagland" src="https://avatars3.githubusercontent.com/u/7661210?v=4&s=117" width="117">](https://github.com/laagland)

[<img alt="cuatl" src="https://avatars1.githubusercontent.com/u/1399392?v=4&s=117" width="117">](https://github.com/cuatl)[<img alt="gianpaj" src="https://avatars3.githubusercontent.com/u/899175?v=4&s=117" width="117">](https://github.com/gianpaj)[<img alt="EdMcBane" src="https://avatars1.githubusercontent.com/u/8511142?v=4&s=117" width="117">](https://github.com/EdMcBane)[<img alt="chriswiggins" src="https://avatars0.githubusercontent.com/u/2830609?v=4&s=117" width="117">](https://github.com/chriswiggins)[<img alt="barryvdh" src="https://avatars2.githubusercontent.com/u/973269?v=4&s=117" width="117">](https://github.com/barryvdh)[<img alt="armno" src="https://avatars3.githubusercontent.com/u/911894?v=4&s=117" width="117">](https://github.com/armno)

[<img alt="archananaik" src="https://avatars2.githubusercontent.com/u/5604248?v=4&s=117" width="117">](https://github.com/archananaik)[<img alt="jakub-g" src="https://avatars2.githubusercontent.com/u/1437027?v=4&s=117" width="117">](https://github.com/jakub-g)[<img alt="shazron" src="https://avatars0.githubusercontent.com/u/36107?v=4&s=117" width="117">](https://github.com/shazron)[<img alt="sclement41" src="https://avatars0.githubusercontent.com/u/443136?v=4&s=117" width="117">](https://github.com/sclement41)[<img alt="hung-doan" src="https://avatars1.githubusercontent.com/u/11371581?v=4&s=117" width="117">](https://github.com/hung-doan)[<img alt="BBosman" src="https://avatars3.githubusercontent.com/u/5115488?v=4&s=117" width="117">](https://github.com/BBosman)

[<img alt="giordanocardillo" src="https://avatars3.githubusercontent.com/u/3403386?v=4&s=117" width="117">](https://github.com/giordanocardillo)[<img alt="mikepsinn" src="https://avatars3.githubusercontent.com/u/2808553?v=4&s=117" width="117">](https://github.com/mikepsinn)[<img alt="AdriVanHoudt" src="https://avatars1.githubusercontent.com/u/2361826?v=4&s=117" width="117">](https://github.com/AdriVanHoudt)[<img alt="alexislg2" src="https://avatars1.githubusercontent.com/u/7933080?v=4&s=117" width="117">](https://github.com/alexislg2)[<img alt="jcesarmobile" src="https://avatars3.githubusercontent.com/u/1637892?v=4&s=117" width="117">](https://github.com/jcesarmobile)[<img alt="nadyaA" src="https://avatars2.githubusercontent.com/u/6064810?v=4&s=117" width="117">](https://github.com/nadyaA)

[<img alt="jdhiro" src="https://avatars0.githubusercontent.com/u/2919453?v=4&s=117" width="117">](https://github.com/jdhiro)[<img alt="edewit" src="https://avatars1.githubusercontent.com/u/51133?v=4&s=117" width="117">](https://github.com/edewit)[<img alt="wildabeast" src="https://avatars1.githubusercontent.com/u/118985?v=4&s=117" width="117">](https://github.com/wildabeast)[<img alt="mkuklis" src="https://avatars2.githubusercontent.com/u/63545?v=4&s=117" width="117">](https://github.com/mkuklis)[<img alt="ashconnell" src="https://avatars2.githubusercontent.com/u/760516?v=4&s=117" width="117">](https://github.com/ashconnell)[<img alt="zwacky" src="https://avatars1.githubusercontent.com/u/1093032?v=4&s=117" width="117">](https://github.com/zwacky)

[<img alt="rakatyal" src="https://avatars2.githubusercontent.com/u/12533467?v=4&s=117" width="117">](https://github.com/rakatyal)[<img alt="jtbdevelopment" src="https://avatars3.githubusercontent.com/u/2074134?v=4&s=117" width="117">](https://github.com/jtbdevelopment)[<img alt="EddyVerbruggen" src="https://avatars1.githubusercontent.com/u/1426370?v=4&s=117" width="117">](https://github.com/EddyVerbruggen)[<img alt="fredgalvao" src="https://avatars2.githubusercontent.com/u/616464?v=4&s=117" width="117">](https://github.com/fredgalvao)[<img alt="bobeast" src="https://avatars0.githubusercontent.com/u/441403?v=4&s=117" width="117">](https://github.com/bobeast)[<img alt="macdonst" src="https://avatars1.githubusercontent.com/u/353180?v=4&s=117" width="117">](https://github.com/macdonst)
