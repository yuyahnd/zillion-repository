FROM ubuntu:20.04

ARG ALT_DIR=/opt/alt

# 日本語環境
RUN apt-get update && apt-get upgrade -y \
 && apt-get install -y tzdata \
    language-pack-ja-base language-pack-ja \
 && locale-gen ja_JP.UTF-8
ENV TZ Asia/Tokyo
ENV LANG ja_JP.UTF-8
ENV LANGUAGE ja_JP:ja
ENV LC_ALL=ja_JP.UTF-8

RUN apt-get install -y \
    python3 \
    python3-pip \
    ffmpeg

WORKDIR $ALT_DIR
COPY requirements.txt $ALT_DIR
RUN pip3 install -r requirements.txt
