FROM ubuntu:18.04

ENV DEBCONF_NOWARNINGS yes

RUN apt-get update && apt-get upgrade -y \
 && apt-get install -y --no-install-recommends \
    wget \
    xz-utils \
    build-essential \
    libglib2.0-dev \
    libgstreamer1.0-dev \
    libgstreamer-plugins-base1.0-dev \
 && apt-get -y clean \
 && rm -rf /var/lib/apt/lists/*

# gst-rtsp-server
RUN cd /opt \
 && wget --no-check-certificate https://gstreamer.freedesktop.org/src/gst-rtsp-server/gst-rtsp-server-1.14.0.tar.xz \
 && tar xvfJ gst-rtsp-server-1.14.0.tar.xz > /dev/null \
 && cd gst-rtsp-server-1.14.0 \
 && ./configure --prefix=/usr \
 && make \
 && make install \
 && cd / \
