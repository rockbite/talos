rm -rf target
jpackage \
--type msi \
--name talos-win-installer \
--dest target/talos \
--temp target/talos-work \
--input build/libs/ \
--icon ./../editor/assets/icon/talos-64x64.ico \
--main-jar editor-desktop-2.0.0-SNAPSHOT.jar \
--main-class com.talosvfx.talos.TalosLauncher \
--file-associations TalosFileAssociations.properties \
--win-dir-chooser \
--win-menu \
--verbose

