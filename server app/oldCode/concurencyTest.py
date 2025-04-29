#server import
from bluedot.btcomm import BluetoothServer
from signal import pause
#input import
from gpiozero import Button
#multi threading import
from multiprocessing import Process
#other
import varListener as listen
from time import sleep
	
def server():
	def disconnect():
		print("disconnect")
	def data_received(data):
		print(data)
		server.send(data)
	server = BluetoothServer(data_received, True, "hci0", 1, "utf-8", True, None, disconnect)
	pause()
	
def liftCheck():
	def off():
		x =(not downLimit.is_pressed or not upLimit.is_pressed)
		if x:
			print("off")
			
	l1 = listen.listener(downLimit.is_pressed)
	l2 = listen.listener(downLimit.is_pressed)
	while True:
		l1.set(downLimit.is_pressed, off)
		l2.set(upLimit.is_pressed, off)
		sleep(0.01)

upLimit = Button(16)
downLimit = Button(12)

p1 = Process(target = server)
p2 = Process(target = liftCheck)
p1.start()
p2.start()
p1.join()
p2.join()
