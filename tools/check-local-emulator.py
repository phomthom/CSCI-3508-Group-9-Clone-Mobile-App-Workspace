#!/usr/bin/env python3
"""Smoke checks for an already-onboarded emulator and the running local mock adapter.
Run from the project root. No production URLs or credentials are used.
"""
import subprocess, xml.etree.ElementTree as ET, re
ADB='/Users/robertrichardson/Library/Android/sdk/platform-tools/adb'
def adb(*args): return subprocess.check_output([ADB,'-s','emulator-5554',*args],text=True)
def ui():
 adb('shell','uiautomator dump /sdcard/studentexchange-ui.xml >/dev/null')
 return ET.fromstring(adb('shell','cat /sdcard/studentexchange-ui.xml'))
def texts(): return [n.get('text') for n in ui().iter('node') if n.get('text')]
def tap(label):
 for n in ui().iter('node'):
  if n.get('text','').casefold()==label.casefold() or n.get('content-desc','').casefold()==label.casefold():
   x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')))
   adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));return
 raise AssertionError('Missing control: '+label)
def check(label):
 assert label in texts(), 'Missing text: '+label
 print('PASS:',label,flush=True)
adb('shell','am','force-stop','com.studentexchange.mobile')
adb('shell','am','start','-n','com.studentexchange.mobile/.MainActivity')
tap('Electronics')
check('No finds yet. Try another search or save a listing with the heart button.')
tap('Textbooks')
# Open condition selector using accessibility hierarchy.
tap('Filters')
tap('All conditions')
check('New');check('Good')
tap('New');tap('Apply')
check('No finds yet. Try another search or save a listing with the heart button.')
tap('Filters');tap('New');tap('Good');tap('Apply')
# Artwork is clickable and appears in the visible card before the title.
tap('Mock calculus textbook')
check('Seller: Mock Seller')
tap('← Campus finds')
tap('Saved');check('Saved finds')
tap('Post');check('Posting is unavailable in this read-only development preview.')
tap('Explore')
print('PASS: Category, condition, detail, and navigation checks',flush=True)
