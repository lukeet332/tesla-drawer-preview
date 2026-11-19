#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME=/home/node/MG4/.gradle-home/jdks/eclipse_adoptium-21-amd64-linux.2
export PATH="$JAVA_HOME/bin:$PATH"
AAPT2=/home/node/MG4/sdk/build-tools/36.0.0/aapt2
AJ=/home/node/MG4/sdk/platforms/android-28/android.jar
rm -rf build gen classes dex
mkdir -p build gen classes dex
$AAPT2 compile --dir res -o build/res.zip
$AAPT2 link -o build/base.apk -I $AJ --manifest AndroidManifest.xml --java gen \
  --min-sdk-version 26 --target-sdk-version 28 --auto-add-overlay build/res.zip
javac -nowarn --release 8 -classpath $AJ -d classes gen/com/mg4/preview/R.java src/com/mg4/preview/MainActivity.java
python3 - <<'PY'
import glob
fs=glob.glob('classes/**/*.class',recursive=True)
open('classes.txt','w').write('\n'.join(fs))
print(len(fs),'classes')
PY
java -cp /home/node/MG4/sdk/build-tools/36.0.0/lib/d8.jar com.android.tools.r8.D8 --min-api 26 --lib $AJ --output dex @classes.txt
python3 - <<'PY'
import zipfile, shutil
shutil.copy('build/base.apk','build/unsigned.apk')
with zipfile.ZipFile('build/unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:
    z.write('dex/classes.dex','classes.dex')
PY
java -jar /home/node/MG4/sdk/build-tools/36.0.0/lib/apksigner.jar sign \
  --ks /home/node/MG4/tmp/repo-compare/Mg4-Launcher-SWI68/keys/platform.p12 \
  --ks-pass pass:android --key-pass pass:android --ks-key-alias platform \
  --out build/preview.apk build/unsigned.apk
python3 - <<'PY'
import zipfile
d=zipfile.ZipFile('build/preview.apk').read('classes.dex').decode('latin-1')
assert 'SoftGlow' in d and 'Skin' in d, 'stale build: new classes missing'
print('verified dex: SoftGlow + Skin present')
PY
