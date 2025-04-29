import serial

#the uart use CR(\r) LF(\n) to indicate end of line
#control cmd "mctl FR FL BR BL \r\n"
#value range from -500.000 to 500.000
def connect(port):
#    try:
        portSer = serial.Serial(port = port,baudrate = 115200)
        return portSer
#    except :
#        return None    

def move2(port:serial.Serial, leftSpeed:int, rightSpeed:int):
    command = "mctl {0} {1} {2} {3} \r\n".format(rightSpeed, leftSpeed, rightSpeed, leftSpeed)
    port.write(bytes(command,'utf-8'))

def move4(port:serial.Serial, frontRight:int, frontLeft: int, backRight:int, backLeft:int):
    command = "mctl {0} {1} {2} {3} \r\n".format(frontRight, frontLeft, backRight, backLeft)
    port.write(bytes(command,'utf-8'))

def stop(port:serial.Serial):
    port.write(b"mctl 0 0 0 0 \r\n")
