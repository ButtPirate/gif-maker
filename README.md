# gif-maker
Small app to convert images into pretty .gif slideshows.

Java is required.

Main feature is usage of different dither FFMPEG filters, as described in [this guide](http://blog.pkh.me/p/21-high-quality-gif-with-ffmpeg.html).

After launch (with default params), .jar will try to:
* list all images in current folder (the folder in which .jar is located).
* back them up in case something goes wrong.
* create a .gif slideshow by using `full` pallete and `bayer5` dither filter.

Order of images in slideshow is based around Windows Explorer sorting (which is not really common): if in doubt about how the images will be ordered, just open Explorer and sort by name.

Main advantage of this app is the ability to produce multiple versions of slideshow, with different combinations of palettegen modes and dither filters.

To use this mode, you have to pass params to .jar file. Open CMD prompt, navigate to folder that contains .jar files and images, and type
`java -jar gif-maker-x.x.jar -A`.

If multiple output files were generated, the first filename in slideshow will be copied to your clipboard.

# Full args reference
When .jar is launched from Command Prompt, there are several flags available via standart Java args (separate by spaces):
* `--all-filters` or `-A` - by default, only `full` palletegen mode, and only `bayer5` dither filter are used. Use this option for creating all possible combinations of all known filters and modes.
* `-D <delay>` - by default, resulting slideshow will show a picture every 2 seconds. This can be overridden with this option. Note, that delay is specified in FFMPEG format (images in second), e.g.: 1 would mean 1 image per second, 0.5 would mean 1 image every two second and so on.
* `--ignore-formats` - by default, an exception will be thrown if at least one of input images is of different format. You can use this arg to ignore this behaviour.
* `-K` - in order to create .gif files, ffmpeg executable will be unpacked to your computer, and will be deleted after conversion. With this key you can keep `ffmpeg.exe` after conversion. This can be useful if you are creating multiple slideshows one after another.

# All palettegen modes and combinations (for `--all-filters` mode)
Palettegen modes: 
* `full`
* `diff`

Dither modes: 
* `none`
* `bayer:bayer_scale=1`
* `bayer:bayer_scale=2`
* `bayer:bayer_scale=5`
* `floyd_steinberg`
* `sierra2`
* `sierra2_4a`

