#server import
from bluedot.btcomm import BluetoothServer
from signal import pause
#input import
from gpiozero import Button
#output import
import l298Control as l298
import uartControl as uart
#multi threading import
import threading
#other
import varListener as listen
from time import sleep
import re

upLimit = Button(16)
downLimit = Button(12)
lastLiftCmd = {"0":0} #/1/-1
lock = threading.Lock() 
port = uart.connect("/dev/ttyUSB0")

#regex result ranging from 0 to the ammount of match string, expected 5
#original regex string: r"-?[0-1]$|(?<=\s)-?50{5}(?!$)|(?<=\s)-?[0-4]\d{0,5}(?!$)|(?<=\s)-?\d{1,5}(?!$)"
def server():
	def data_received(data):
		if(data[0:4]) != "move":
			return
		tempData = data[6:]
		print(tempData)
		if(data[5]) == "w":
			#expected str " 500.000 500.000 500.000 500.000"
			request = re.findall(r"(?<=\s)-?50{5}|(?<=\s)-?[0-4]\d{0,5}|(?<=\s)-?\d{1,5}", tempData)
			if len(request) != 4:
				print("invalid ammount of parameter")
				return
			#expected cmd order "FR FL BR BL"| L = left | R = right | F = front | B = back
			uart.move4(port, request[0], request[1], request[2], request[3])
			
		if(data[5]) == "l":
			#expected str lift " 1/0/-1"
			request = re.search(r"^\s-?[0-1]", tempData).string
			print(request)
			if request == None or request == "" or request == " ":
				print("incorrect data format")
			#due to incorrect marking on the switch port "is_pressed = true" actually mean not press
			match request:
				case " 1\r\n":
					if not upLimit.is_pressed:
						l298.allOff()
						print("Lift has reach maximum height")
						return
					#l298.moveOne(False, 100)
					with lock:
						lastLiftCmd["0"]= 1
				case " 0\r\n":
					l298.allOff()
					with lock:
						lastLiftCmd["0"] = 0
				case " -1\r\n":
					if not downLimit.is_pressed:
						l298.allOff()
						print("Lift has reach minimum height")
						return
					#l298.moveOne(True, 70)
					with lock:
						lastLiftCmd["0"] = -1
				case _:
					print("Invalid lift input")
					return
		
	def disconnect():
		uart.move4(port, 0, 0, 0, 0)
		l298.allOff()
		print("connection lost shutting down all motor")
		
	server = BluetoothServer(data_received, True, "hci0", 1, "utf-8", True, None, disconnect)
	pause()
	
def liftCheck():
	def off1():
		with lock:
			if ((not downLimit.is_pressed) and lastLiftCmd["0"] == -1):
				print("Automatic shut off " + str(lastLiftCmd["0"]))
				l298.allOff()
		
	
	def off2():
		with lock:
			if ((not upLimit.is_pressed) and lastLiftCmd["0"] == 1):
				print("Automatic shut off " + str(lastLiftCmd["0"]))			
				l298.allOff()
	
			
	l1 = listen.listener(downLimit.is_pressed)
	l2 = listen.listener(downLimit.is_pressed)
	while True:
		l1.set(downLimit.is_pressed, off1)
		l2.set(upLimit.is_pressed, off2)
		sleep(0.01)

p1 = threading.Thread(target = server)
p2 = threading.Thread(target = liftCheck)
p1.start()
p2.start()
p1.join()
p2.join()
