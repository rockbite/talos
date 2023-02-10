rm -rf target
jpackage \
--type pkg \
--name TalosVFX \
--dest target/talos \
--temp target/talos-work \
--input build/libs/ \
--icon ./jpackage-mac-res/launcher.icns \
--main-jar bootstrap.jar \
--main-class com.talosvfx.talos.Start \
--resource-dir jpackage-mac-res \
--file-associations jpackage-mac-res/TalosFileAssociations.properties \
--java-options "-XstartOnFirstThread" \
--java-options "-splash:$APPDIR/splash.png" \
--verbose

