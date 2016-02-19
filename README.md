# JGet
horribly messy and inefficient 8chan image fetcher

## Usage
Invoke `java -jar JGet.jar --url %url%` from the command line where %url% is a link to an 8chan thread. Don't expect it to work.

Adding flag `--save-html` will save the HTML file.

If you just want to save the HTML to download the images later use flag `--no-download-images` with `--save-html`.

Using flag `--use-html %path-to-file%`, where %path-to-file% is a HTML file obtained by `--save-html` or other means, will parse 
the provided file instead of a URL.

A proxy can be used by adding flag `--use-proxy %ip% %port%`, where %ip% is the IP of the proxy server and %port% is the port.
