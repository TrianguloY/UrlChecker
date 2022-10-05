# Retrieve parameters
fileName=$1
fileURL=$2
path=$3
hashURL=$4

# Temporal folder to work on
# Needed in case a file is updated in the main folder, and the hash checking fails
downloadFolder="./.file_updater_action_temporal_folder/"
mkdir $downloadFolder
tempPath="$downloadFolder$fileName"

# Store checksum
curlHashExit=1
if [ "$path" != "" ]; then
  checksum=$(curl $hashURL)
  curlHashExit=$?
fi

# Avoid download of same file
oldFileChecksum=$(sha256sum "$path$fileName")
if [ "$oldFileChecksum" = "$checksum  $path$fileName" ]; then
  echo "Current file checksum is the same as the new one"
  exit 1
fi

# Download file
curl "$fileURL" -o $tempPath
curlFileExit=$?

# If both downloads were succesful
if [[ $curlFileExit -eq 0 && $curlHashExit -eq 0 ]]; then
  # Checksum of file
  dlFileChecksum=$(sha256sum $tempPath)
  # If matches
  if [ "$dlFileChecksum" = "$checksum  $tempPath" ]; then
    mv $tempPath "$path$fileName"
    # No need to check exit code, if mv fails GitHub actions will too
  else
    # If not, failure
    echo "Hash does not match" >&2
    exit 2
  fi
else
  # If not, failure
  echo "Downloads failed" >&2
  exit 3
fi