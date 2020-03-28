#!/bin/python

'''
SETUP:

    -   -->     GND     -->     PIN6
    +   -->     5V      -->     PIN4
    S   -->     GPIO18  -->     PIN12

'''

import RPi.GPIO as GPIO
import subprocess
import time
import sys
import threading
from Queue import Queue
import logging
import boto3
from botocore.exceptions import ClientError


'''
Runs darknet on recorded video
'''
def run_darknet():
    global q
    global darknet
    global lock

    obj_list = []
    while 1:
        print("darknet thread")
        lock.acquire()
        video_file = q.get()
        lock.release()
        print("running darknet on: ", video_file)
        darknet = 1
        video_name = video_file[video_file.rindex('/')+1:]
        print(video_name)
        result = open('/home/pi/darknet/results/' + video_name, 'r')
        subprocess.call(['/home/pi/darknet/darknet', 'detector', 'demo', '/home/pi/darknet/cfg/coco.data', '/home/pi/darknet/cfg/yolov3-tiny.cfg', '/home/pi/darknet/yolov3-tiny.weights', '/home/pi/darknet/test_video.h264'], stdout=result)
        flag = 0
        for line in result:
            if line == "Objects:\n":
                flag = 1
            elif "FPS" in line:
                flag = 0
            elif flag == 1 and line != '\n' and line != "\x1b[2J\x1b[1;1H\n":
                obj = line.split(':')[0]
                if obj not in obj_list:
                    obj_list.append(obj)
 
        result.close()
        print(obj_list)
        darknet =0
        if obj_list is None:
            obj_list.append("No Object Detected")
        '''
        upload result to S3
        '''
        result_string = ""
        for obj in obj_list:
            result_string = result_string + ',' + obj

        result_string = result_string[1:]
        f = open('/home/pi/darknet/result_strings/' + video_name, 'w')
        f.write(result_string)
        f.close()

        #TODO:change bucket name
        bucket = "ccfoebucket"
        s3_client = boto3.client('s3')
        try:
            response = s3_client.upload_file('/home/pi/darknet/result_strings/' + video_name, bucket, video_name)
        except ClientError as e:
            logging.error(e)
        print("darknet finished")
    #.exit()

'''
Uploads video to S3
'''
def upload_to_s3():
    global q
    global lock
    bucket = "ccfoebucket"
	
    s3_client = boto3.client('s3')
    
    while 1:
        print("upload thread")
        lock.acquire()
        video_file = q.get()
        lock.release()

        video_name = video_file[video_file.rindex('/')+1:]
		
        print("uploading to s3: ", video_name)
        #time.sleep(30)
        try:
            time.sleep(30)
            #response = s3_client.upload_file(video_file, bucket, video_name)
        except ClientError as e:
            logging.error(e)

        print("uploading finished")
    

sensor = 11

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)
GPIO.setup(sensor, GPIO.IN)

on = 0
off = 0
flag = 0
count = 0
darknet = 0
upload_threads = []
q = Queue(maxsize = 50)
lock = threading.Lock()

darknet_thread = threading.Thread(target = run_darknet)
darknet_thread.daemon = True
darknet_thread.start()

for i in range(2):
    u = threading.Thread(target = upload_to_s3)
    u.daemon = True
    upload_threads.append(u)


while flag == 0:
    i=GPIO.input(sensor)
    print("input from sensor: ", i)
    if i == 0:
        if flag == 1:
            off = time.time()
            diff = off - on
            print 'time: ' + str(diff%60) + ' sec'
            print ''
            flag = 0
        print "No intruders"
        time.sleep(1)
    elif i == 1:
        if flag == 0:
            print "Intruder detected"
            on = time.time()
            #flag = 1
            video_file = '/home/pi/facedetect/videos/video' + str(count) + '.h264'
            subprocess.call(['sudo','raspivid','-o', video_file,'-t','5000'])
            print("generated video file: " + video_file)
            #print(q.qsize())
            q.put(video_file)
            print(q.qsize())
            count +=1
			
            if darknet ==1:
                for t in upload_threads:
                    if t.ident == None:
                        print("starting upload thread")
                        t.start()

