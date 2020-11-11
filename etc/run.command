#!/bin/bash
cd "$(dirname "$0")"
java -Xss1024k -Xms512m -Xmx1024m -jar hafen.jar -U https://game.havenandhearth.com/hres/ game.havenandhearth.com
