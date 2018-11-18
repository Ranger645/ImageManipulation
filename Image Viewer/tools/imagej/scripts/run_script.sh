#!/bin/bash

SCRIPT_PATH=$1

printf -v AUGMENTED_PATH '%q' "$SCRIPT_PATH"

# Outputting information:
echo "Running Script: [$AUGMENTED_PATH]"
if [ ! -f $AUGMENTED_PATH ]; then
    echo "ERROR: Script not found!"
    exit
fi

# Running the script itself:
/Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --ij2 -batch "$SCRIPT_PATH" "$2"