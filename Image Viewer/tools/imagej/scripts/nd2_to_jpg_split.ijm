/*
 * Takes in a single nd2 file argument to split into channels and dump
 * the individual jpeg layers into the temp folder.
 */

arg = getArgument();
run("Bio-Formats Windowless Importer", "open=[arg]");
run("Make Composite");
run("Split Channels");

// we need to know only how many frames there are
getDimensions(dummy, dummy, dummy, dummy, nFrames);

// for each frame...
frame = 0;
while (nImages > 0) {
    frame++;
    saveAs("Jpeg", "temp/" + frame + ".jpg");
    close();
}
