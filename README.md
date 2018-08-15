# gif-maker
Small app to convert images into pretty .gif slideshows.

Main feature is usage of different dither FFMPEG filters, as described in [this guide](http://blog.pkh.me/p/21-high-quality-gif-with-ffmpeg.html).

After launch, .jar will try to:
* list all images in current folder (the folder in which .jar is located).
* back them up in case something goes wrong.
* create multiple .gifs based on combination of different palettegen and dither filter modes.
* save name of the first image to clipboard - for renaming version of your choice.

Order of images in slideshow is based around Windows Explorer sort (which is not really common): if in doubt about how the images are ordered, just open Explorer and sort by name.

When .jar is launched from Command Promt, there is several flags available via standart Java args (separate by spaces):
* `--all-filters` or `-A` - by default, only `full` palletegen mode is used, and only several filters. Use this option for creating all possible combinations of all known filters.
* `-D <delay>` - by default, resulting slideshow will show a picture every 2 seconds. This can be overriden with this option. Note, that delay is specified in FFMPEG format (images in second), e.g.: 1 would mean 1 image per second, 0.5 would mean 1 image every two second and so on.
* `--ignore-formats` - by default, an exception will be thrown if at least one of input images is of different format. You can use this to ignore this behaviour.
