FROM python:3.8.5

# 言語設定
RUN apt-get update && apt-get install -y locales \
 && localedef -i ja_JP -c -f UTF-8 -A /usr/share/locale/locale.alias ja_JP.UTF-8
ENV LANG ja_JP.UTF-8

# タイムゾーン設定
RUN apt-get update && apt-get upgrade -y \
 && apt-get install -y tzdata
ENV TZ Asia/Tokyo
