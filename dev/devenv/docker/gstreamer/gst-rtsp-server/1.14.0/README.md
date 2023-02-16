# GStreamer RTSP Server Ver.1.14.0

## build
```
docker build -t gst-rtsp-server:1.14.0 .
```

## run
```
docker run --rm -it gst-rtsp-server:1.14.0 /bin/bash
```

### RTSP sample command
```
/opt/gst-rtsp-server-1.14.0/examples/test-launch '( videotestsrc ! x264enc ! rtph264pay name=pay0 pt=96 )'
```