from gpiozero import Button
from time import sleep
import varListener as listener

#button = Button(16)
button = Button(12)

l = listener.listener()
def func(x):
	print(x)
while True:
	l.set(not button.is_pressed,func)
	sleep(0.01)
#while True:
#	if not button.is_pressed:
#		print("click")
#		sleep(0.5)
