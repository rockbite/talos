rm -rf target
jpackage \
--type msi \
--name TalosVFX \
--dest target/talos \
--temp target/talos-work \
--input build/libs/ \
--icon ./../editor/assets/icon/talos-64x64.ico \
--main-jar bootstrap.jar \
--main-class com.talosvfx.talos.Start \
--file-associations jpackage-win-res/TalosFileAssociations.properties \
--win-dir-chooser \
--win-menu \
--verbose

