#cd "C:\Users\tomco\Coding\rockbite\talos\editor-desktop-bootstrap\fakedir\fakeraws"
cd /var/www/editor.talosvfx.com/raw-editor-binaries

backdelim='editor-desktop-'
frontdelim='-config.xml'

for i in *.xml; do
  [ -f "$i" ] || break
  echo "TESTING ON $i"
  STRIPPEDVERSION=$(awk -F$frontdelim '{print $1}' <<< "$i")
  STRIPPEDVERSION=$(awk -F$backdelim '{print $2}' <<< "$STRIPPEDVERSION")
#  found a config, got the version, lets find the binary, if it exists we chuck it to the dir along with the config

#  my awk is bad
  MAJOR=$(awk -F. '{print $1}' <<< "$STRIPPEDVERSION")
  MINOR=$(awk -F. '{print $2}' <<< "$STRIPPEDVERSION")
  PATCH=$(awk -F. '{print $3}' <<< "$STRIPPEDVERSION")

  SNAPSHOT=$(awk -F- '{print $2}' <<< "$PATCH")

  SNAPSHOTSIZE=${#SNAPSHOT}
  if [ "$SNAPSHOTSIZE" == "0" ]; then
    SNAPSHOT=false
  else
    PATCH=$(awk -F-SNAPSHOT '{print $1}' <<< "$PATCH")
    SNAPSHOT=true
  fi

  CHANNEL="$MAJOR.$MINOR"
  if [ "$SNAPSHOT" == "true" ]; then
    CHANNEL="$CHANNEL-SNAPSHOT"
  fi

  # Make the dir if it doesnt exist
  if [ -d "../channels/$CHANNEL" ]; then
    echo "Channel folder exists"
  else
    echo "Channel folder doesn't exist, creating"
    mkdir -p "../channels/$CHANNEL"
  fi

  BASEFILETOCOPY="editor-desktop-$MAJOR.$MINOR.$PATCH"
  BINARYTOCOPY=$BASEFILETOCOPY
  CONFIGTOCOPY=$BASEFILETOCOPY

   if [ "$SNAPSHOT" == "true" ]; then
     BINARYTOCOPY="$BINARYTOCOPY-SNAPSHOT"
     CONFIGTOCOPY="$CONFIGTOCOPY-SNAPSHOT"
   fi
   BINARYTOCOPY="$BINARYTOCOPY.jar"
   CONFIGTOCOPY="$CONFIGTOCOPY-config.xml"

   #test if exists
   if [[ ! -f "$BINARYTOCOPY" || ! -f "$CONFIGTOCOPY" ]]; then
      echo "Error - Did not find $BINARYTOCOPY or $CONFIGTOCOPY"
      exit 1;
   fi

  echo "COPYING $BINARYTOCOPY and $CONFIGTOCOPY to $CHANNEL"

  cp $BINARYTOCOPY "../channels/$CHANNEL"
  cp $CONFIGTOCOPY "../channels/$CHANNEL/config.xml"

  echo "$STRIPPEDVERSION" > "../channels/$CHANNEL/latestVersion.txt"


  #config is always the latest on in the dir so we rename it to config.xml

done


# Lets make the repo json

# We go over all the folders in the directory above, each ones name is the channel id, we store that along with the latest
# version that we can find inside that dir

VERSIONS=()
LATESTVERSIONS=()

cd "../channels"

for d in */; do
  LATESTVERSIONFILE="${d}latestVersion.txt"
  LATESTVERSION=$(head -n 1 "$LATESTVERSIONFILE")

  VERSIONTRIMMED=${d::-1}

  VERSIONS+=($VERSIONTRIMMED)
  LATESTVERSIONS+=($LATESTVERSION)

done

JSONOBJECTS=()

len=${#VERSIONS[@]}
for (( i=0; i<$len; i++ )); do
  JSON="{\"versionIdentifier\": \"${VERSIONS[$i]}\", \"latestVersionString\": \"${LATESTVERSIONS[$i]}\"}"
  JSONOBJECTS+=("$JSON")
done

REPOJSON="{\"versions\":["

len2=${#JSONOBJECTS[@]}
last=$(expr $len2 - 1)
for (( i=0; i<$len2; i++)); do
    REPOJSON=$REPOJSON${JSONOBJECTS[$i]}
    if [ ! "$i" == "$last" ]; then
      REPOJSON+=","
    fi
done

REPOJSON="$REPOJSON]}"
echo $REPOJSON > repo.json


echo "Repo json written"

#Clean up the dir

cd ../
rm -rf raw-editor-binaries

echo "Cleaned directories"
