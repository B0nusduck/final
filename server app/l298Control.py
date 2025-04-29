import RPi.GPIO as gpio

#disable unecessary warnings, turn off during testing and debugging
gpio.setwarnings(False)

#pin number assignment
l298_enA = 11
l298_in1 = 13
l298_in2 = 15

l298_in3 = 16
l298_in4 = 18
l298_enB = 22

leftIFSensor = 31
rightIFSensor = 33

gpio.setmode(gpio.BOARD)

#Set output
gpio.setup(l298_enA,gpio.OUT)
gpio.setup(l298_in1,gpio.OUT)
gpio.setup(l298_in2,gpio.OUT)

gpio.setup(l298_in3,gpio.OUT)
gpio.setup(l298_in4,gpio.OUT)
gpio.setup(l298_enB,gpio.OUT)
#adjust rotation frequency
enA=gpio.PWM(l298_enA,1000)
enB=gpio.PWM(l298_enB,1000)
#adjust engine power
#voltage recommended by manufacturer 12v(33%~11.9v)
#enA.start(100)
#enB.start(100)

#on
def moveOne(direction:bool, power:int):
	if direction:
		enA.start(power)
		gpio.output(l298_in1,gpio.HIGH)
		gpio.output(l298_in2,gpio.LOW)
	else:
		enA.start(power)
		gpio.output(l298_in1,gpio.LOW)
		gpio.output(l298_in2,gpio.HIGH)

def moveTwo(direction:bool, power:int):
	if direction:
		enB.start(power)
		gpio.output(l298_in3,gpio.HIGH)
		gpio.output(l298_in4,gpio.LOW)
	else:
		enB.start(power)
		gpio.output(l298_in3,gpio.LOW)
		gpio.output(l298_in4,gpio.HIGH)

#off
def oneOff():
	gpio.output(l298_in1,gpio.LOW)
	gpio.output(l298_in2,gpio.LOW)

def twoOff():
	gpio.output(l298_in3,gpio.LOW)
	gpio.output(l298_in4,gpio.LOW)

def allOff():
	oneOff()
	twoOff()

def cleanUp():
	gpio.cleanup()
