outDir="out-win"
binaryName="Talos"
extension=".exe"
version="1.4.1"
sourceJar="../build/libs/editor-"$version"-SNAPSHOT.jar"
mainClass="com.talosvfx.talos.TalosLauncher"
portableInstall="portable-talos-"$version"-win-x64.zip"

rm -rf $outDir
java -jar packr-all-4.0.0.jar \
     --platform windows64 \
     --jdk jdk/OpenJDK8U-jre_x64_windows_hotspot_8u292b10.zip \
     --useZgcIfSupportedOs \
     --executable $binaryName \
     --classpath $sourceJar \
     --mainclass $mainClass \
     --vmargs Xmx1G \
     --resources \
     --verbose \
     --output $outDir


pathToExe=$outDir"/"$binaryName$extension
echo $pathToExe
./rcedit-x64.exe $pathToExe --set-icon "talos-64x64.ico"


rm $portableInstall
cd $outDir
zip -r ../$portableInstall ./
