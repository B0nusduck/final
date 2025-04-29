import bluetooth
import re
#import subprocess
#from subprocess import PIPE

def Controller():
	print('initializing')
	port = 0
	serverSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
	serverSocket.bind(('',port))

	print('connecting')

	serverSocket.listen(1)
	clientSocket, clientAddressPort = serverSocket.accept()

	print('connection success')

	while true:
		try:
			data = clientSocket.recv()
			recievedData = data.decode('ascii')
			recievedStr = str(recieved)
			print('recieved: ' + recievedStr)
			#if(recievedStr)[0] == 'a'
			#	tempData = recievedStr[1:len(recieved)]
			#regex result ranging from 0 to the ammount of match string
			#expected 5
			#request = re.findall(r"-?[0-1]$|(?<=\s)-?50{5}(?!$)|(?<=\s)-?[0-4]\d{0,5}(?!$)|(?<=\s)-?\d{1,5}(?!$)", tempData)
			#print("parsed command: ")
			#for command in request:
			#	print(command)
		except:
			#clientSocket.close()
			#serverSocket.close()
			import traceback
			traceback.print_exc()
			#break
	clientSocket.close()
	serverSocket.close()

if __name__ == "__main__":
	Controller()
