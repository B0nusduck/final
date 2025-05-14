using InTheHand.Net.Bluetooth;
using InTheHand.Net.Sockets;
using InTheHand.Net;
using InTheHand.Bluetooth;
using System.Net.Sockets;
using System.Text;
using Microsoft.Maui.Controls;
using System.Runtime.CompilerServices;

namespace bluetoothController
{
    public partial class MainPage : ContentPage
    {
        private readonly int maxMotorStrength = 500000;
        private float motorPower = 1;
        private int motorStrength;
        private ImageButton buttonWF = new ImageButton();
        private ImageButton buttonWL = new ImageButton();
        private ImageButton buttonWR = new ImageButton();
        private ImageButton buttonWB = new ImageButton();
        private ImageButton buttonLU = new ImageButton();
        private ImageButton buttonLD = new ImageButton();
        private Button connectBTN = new Button();
        private BluetoothClient? Mainclient;
        
        public MainPage()
        {
            InitializeComponent();
            buttonWF = (ImageButton)FindByName("WheelForwardBTN");
            buttonWL = (ImageButton)FindByName("WheelLeftBTN");
            buttonWR = (ImageButton)FindByName("WheelRightBTN");
            buttonWB = (ImageButton)FindByName("WheelBackBTN");
            buttonLU = (ImageButton)FindByName("LiftUpBTN");
            buttonLD = (ImageButton)FindByName("LiftDownBTN");
            connectBTN = (Button)FindByName("BluetoothConnectionBTN");
            motorStrength = (int)(maxMotorStrength * motorPower);
        }
        private async Task<BluetoothClient?> GetMainclient()
        {
            if(Mainclient != null)
                return Mainclient;
            else
            {
                try
                {
                    return Mainclient = new BluetoothClient();
                }
                catch (PlatformNotSupportedException ex)
                {
                    await AlertException(ex);
                    return Mainclient = null;
                }
                catch (Exception ex)
                {
                    await AlertException(ex);
                    return null;
                }

            }
            
        }
        private async Task AlertException(Exception ex)
        {
            var test1 = ex.GetType();
            switch (ex.GetType().Name)
            {
                case ("PlatformNotSupportedException"):
                    {
                        await DisplayAlert("No bluetooth device detected", "please turn on your bluetooth so the app can work", "ok");
                        break;
                    }
                case ("SocketException"):
                    {
                        await DisplayAlert("vehicle not responding", "try restarting the vehicle onboard processor", "ok");
                        break;
                    }
                default:
                    {
                        await DisplayAlert("Something went wrong", "the app encountered unexpected error please try again", "ok");
                        break;
                    }

            }
        }

        private void MotorStrenghtEntry(object sender, TextChangedEventArgs e)
        {
            try
            {
                float input = float.Parse(e.NewTextValue);
                if (input > 100)
                {
                    input = 100;
                    entry.Text = input.ToString();
                }
                if (input < 0)
                {
                    input = 0;
                    entry.Text = input.ToString();
                }

                motorPower = input / 100;
                motorStrength = (int)(maxMotorStrength * motorPower);
            }
            catch(Exception ex)
            {
                entry.Text = e.OldTextValue;
            }
        }

        private void BTNPressed(object sender, EventArgs e)
        {
            if (sender.GetType() == typeof(ImageButton))
            {
                ImageButton button = (ImageButton)sender;
                button.BackgroundColor = Colors.LightGray;
            }
            else
            {
                Button button = (Button)sender;
                button.BackgroundColor = Colors.LightGray;
            }         
        }

        private void BTNReleased(object sender, EventArgs e)
        {
            ImageButton button = (ImageButton)sender;
            button.BackgroundColor = null;
        }

        public async Task<BluetoothClient?> ConnectBluetooth()
        {
            BluetoothAddress deviceAddress = BluetoothAddress.Parse("D8:3A:DD:64:82:28");

            try
            {
                Mainclient?.Connect(deviceAddress, BluetoothService.SerialPort);


                if (Mainclient != null && !Mainclient.Connected)
                {
                    await DisplayAlert("connection failed", null, "ok");
                    return null;
                }
                else
                {
                    return Mainclient;
                }
            }
            catch (SocketException ex)
            {
                await AlertException(ex);
                return null;
            }
            catch (Exception ex) 
            {
                await AlertException(ex);
                return null;
            }
        }

        public async Task<bool> SentData(BluetoothClient? client, string data)
        {
            try
            {
                if (client != null && client.Connected)
                {
                    NetworkStream stream = client.GetStream();
                    byte[] dataBytes = Encoding.UTF8.GetBytes(data);
                    stream.Write(dataBytes);
                    return true;
                }
                else
                {
                    connectBTN.BackgroundColor = (Mainclient != null && Mainclient!.Connected) ? Colors.LightGreen : Colors.Red;
                    await DisplayAlert("connection lost", "try reconnecting to the vehicle", "ok");
                    return false;
                }
            }
            catch (Exception ex)
            {
                connectBTN.BackgroundColor = (Mainclient != null && Mainclient!.Connected) ? Colors.LightGreen : Colors.Red;
                await AlertException(ex);
                return false;
            }
        }

        public async Task<string?> GetData(BluetoothClient? client)
        {
            try
            {

                if (client != null && client.Connected)
                {
                    NetworkStream stream = client.GetStream();
                    byte[] receiveBuffer = new byte[1024];
                    int bytesRead = stream.Read(receiveBuffer, 0, receiveBuffer.Length);
                    string receivedData = Encoding.UTF8.GetString(receiveBuffer, 0, bytesRead);

                    return receivedData;
                }
                else
                {
                    connectBTN.BackgroundColor = (Mainclient != null && Mainclient!.Connected) ? Colors.LightGreen : Colors.Red;
                    await DisplayAlert("connection lost", "try reconnecting to the vehicle", "ok");
                    return null;
                }
            }
            catch (Exception ex)
            {
                connectBTN.BackgroundColor = (Mainclient != null && Mainclient!.Connected) ? Colors.LightGreen : Colors.Red;
                await AlertException(ex);
                return null;
            }
        }

        private async void BluetoothConnectionBTN_Clicked(object sender, EventArgs e)
        {
            connectBTN.IsEnabled = false;

            BluetoothClient? client = await GetMainclient();
            if (client != null && client.Connected)
            {
                Mainclient!.Close();
                Mainclient!.Dispose();
                Mainclient = new BluetoothClient();
            }
            else
            {
                client = await ConnectBluetooth();
            }

            connectBTN.BackgroundColor = (Mainclient != null && Mainclient!.Connected) ? Colors.LightGreen : Colors.Red;
            connectBTN.IsEnabled = true;
        }

        //move w fr fl br bl
        private void WheelBTN_Released(object sender, EventArgs e)
        {
            buttonWF.IsEnabled = true;
            buttonWL.IsEnabled = true;
            buttonWR.IsEnabled = true;
            buttonWB.IsEnabled = true;
            BTNReleased(sender, e);
            if(Mainclient != null)
            {
                SentData(Mainclient, $"move w 0 0 0 0");
            }
            
            
        }

        private void WheelForwardBTN_Pressed(object sender, EventArgs e)
        {
            buttonWL.IsEnabled = false;
            buttonWR.IsEnabled = false;
            buttonWB.IsEnabled = false;
            BTNPressed(sender, e);
            SentData(Mainclient, $"move w {motorStrength} {motorStrength} {motorStrength} {motorStrength}");
        }


        private void WheelLeftBTN_Pressed(object sender, EventArgs e)
        {
            buttonWF.IsEnabled = false;
            buttonWR.IsEnabled = false;
            buttonWB.IsEnabled = false;
            BTNPressed(sender, e);
            SentData(Mainclient, $"move w {-motorStrength} {motorStrength} {-motorStrength} {motorStrength}");
        }

        private void WheelRightBTN_Pressed(object sender, EventArgs e)
        {
            buttonWF.IsEnabled = false;
            buttonWL.IsEnabled = false;
            buttonWB.IsEnabled = false;
            BTNPressed(sender, e);
            SentData(Mainclient, $"move w {motorStrength} {-motorStrength} {motorStrength} {-motorStrength}");
        }

        private void WheelBackBTN_Pressed(object sender, EventArgs e)
        {
            buttonWF.IsEnabled = false;
            buttonWL.IsEnabled = false;
            buttonWR.IsEnabled = false;
            BTNPressed(sender, e);
            SentData(Mainclient, $"move w {-motorStrength} {-motorStrength} {-motorStrength} {-motorStrength}");
        }
//move l 1/0/-1
        private void LiftBTN_Released(object sender, EventArgs e)
        {
            buttonLU.IsEnabled = true;
            buttonLD.IsEnabled = true;
            BTNReleased(sender, e);
            if(Mainclient != null)
            {
                SentData(Mainclient, "move l 0");
            }

        }

        private void LiftUpBTN_Pressed(object sender, EventArgs e)
        {
            buttonLD.IsEnabled = false;
            BTNPressed(sender, e);
            SentData(Mainclient, "move l 1");
        }

        private void LiftDownBTN_Pressed(object sender, EventArgs e)
        {
            buttonLU.IsEnabled = false;
            BTNPressed(sender, e);
            SentData(Mainclient, "move l -1");
        }
    }

}
