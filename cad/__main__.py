#!/usr/bin/env python3
from argparse import ArgumentParser
from argparse import _SubParsersAction

def execute() -> None:
    """サブコマンドと起動パラメタを解析して実行します"""
    parser = ArgumentParser(description='cad command line')
    sub = parser.add_subparsers()

    add_url(sub)

    args = parser.parse_args()
    if hasattr(args, 'handler'):
        args.handler(args)
    else:
        parser.print_help()

def add_url(sub: _SubParsersAction) -> None:
    """URL コマンドを追加します

    Parameters
    ----------
    sub : _SubParsersAction
        サブパーサー
    """
    parser = sub.add_parser('url', help='see url -h')
    parser.add_argument('-o', '--open', type=str, help='open url')
    parser.set_defaults(handler=url)

def url(args) -> None:
    """URL コマンドを実行します

    Parameters
    ----------
    args : _type_
        起動パラメタ
    """
    print(args)

def main() -> None:
    """cad コマンドを実行します"""
    execute()

if __name__ == '__main__':
    main()
