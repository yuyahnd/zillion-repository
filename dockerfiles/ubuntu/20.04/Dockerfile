FROM ubuntu:20.04

# 日本語設定
RUN apt-get update && apt-get upgrade -y \
 && apt-get install -y language-pack-ja-base language-pack-ja \
 && locale-gen ja_JP.UTF-8
ENV LANG ja_JP.UTF-8
ENV LANGUAGE ja_JP:ja
ENV LC_ALL=ja_JP.UTF-8
