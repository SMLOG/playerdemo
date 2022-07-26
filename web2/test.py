# -*- coding: utf-8 -*-

from urllib.request import *
import urllib.parse
import json
import re
from lxml import etree
avid='13762839'
logincookies=''
cookies=''
loginheaders = {
    'Cookie':logincookies
}
#登录验证的Cookie
downloadheaders={
    'Cookie':cookies
}
#下载验证的Cookie
pageurl='http://www.ibilibili.com/video/av'+avid
request=Request(pageurl,headers=loginheaders)
response=urlopen(request)
source=response.read()
html=etree.HTML(source)
titles=html.xpath('//*[@class="list-group-item"]')
pages=len(titles)
f=open('log.txt','w+')
for i in range(pages):
    url='http://api.bilibili.com/playurl?callback=cb&aid={}&page={}&platform=html5&quality=1&vtype=mp4&type=jsonp&cb=cb&_=0'.format(avid,str(i+1))
    request=Request(url,headers=downloadheaders)
    response=urlopen(request)
    htmlcode = (response.read().decode())
    jsoncode=htmlcode[htmlcode.find('(')+1:htmlcode.find(')')]
    restext=json.loads(jsoncode)
    url=restext['durl'][0]['url']
    print(url)
    #这里不知道为什么最短匹配失效了,所以把原网址做了切分再进行正则搜索
    pattern=re.compile("\/(.*?\.mp4)")
    filename=re.findall(pattern,url[url.rfind('/'):])
    # 井号作为分隔符
    f.write('%s#%s\n'%(filename[0],titles[i].text.strip()+'.mp4'))
f.close()