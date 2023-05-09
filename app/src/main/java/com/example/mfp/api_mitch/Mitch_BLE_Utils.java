/** 
 * @file Mitch_BLE_Utils.cs
 * 
 * This file is part of Mitch_API library.

 * Mitch_API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Mitch_API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Nome-Programma.If not, see<http://www.gnu.org/licenses/>.

 * Copyright (c) 2020 by 221e srl.
 * 
 */
/// <summary>
/// Class <c>Mitch_BLE_Utils</c> defines encoding and deconding basic functionalities based on Bluetooth Low Energy communication protocol specifications.
/// </summary>
public class Mitch_BLE_Utils
{
    public static int byteArrayToInt(byte[] b) 
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a)
    {
        return new byte[] {
            (byte) ((a >> 24) & 0xFF),
            (byte) ((a >> 16) & 0xFF),   
            (byte) ((a >> 8) & 0xFF),   
            (byte) (a & 0xFF)
        };
    }

    public static byte[] floatToByteArray(float a)
    {
        int intBits = Float.floatToIntBits(a);
        return new byte[] {
            (byte) ((intBits >> 24) & 0xFF), 
            (byte) ((intBits >> 16) & 0xFF),
            (byte) ((intBits >> 8) & 0xFF),
            (byte) (intBits & 0xFF) 
        };  
    }

    private static byte[] doubletoBytes(double a) {
        long data = Double.doubleToRawLongBits(a);
        return new byte[]{
            (byte) ((data >> 56) & 0xFF),
            (byte) ((data >> 48) & 0xFF),
            (byte) ((data >> 40) & 0xFF),
            (byte) ((data >> 32) & 0xFF),
            (byte) ((data >> 24) & 0xFF),
            (byte) ((data >> 16) & 0xFF),
            (byte) ((data >> 8) & 0xFF),
            (byte) ((data >> 0) & 0xFF),
        };
    }

    // region COMMAND IMPLEMENTATION

    // CMD_ACK = 0x00,                 //!< CMD_ACK
    // Not implemented!

    /// <summary>
    /// Encoding Shutdown command
    /// @return A byte array of 20 elements containing the encoded shutdown command to be sent.
    /// </summary>
    public static byte[] Cmd_Shutdown()
    {
        // Force the system to enter STANDBY mode
        byte[] buffer = Cmd_SetState(Mitch_HW.SystemState.SYS_STANDBY, null);
        return buffer;
    }

    /// <summary>
    /// Allows to retrieve the current system state
    /// @return A byte array of 20 elements containing the encoded get state command to be sent.
    /// </summary>
    public static byte[] Cmd_GetState()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        // Header
        buffer[0] = (byte)(Mitch_HW.Command.CMD_STATE.code + 0x80);
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Allows to set a specified state
    /// @param state system state to be set. Default is SYS_NULL.
    /// @param stateParams system state parameters, if required. Default is null.
    /// @return A byte array of 20 elements containing the encoded set state command and the necessary parameters to be sent.
    /// </summary>
    public static byte[] Cmd_SetState(Mitch_HW.SystemState state, byte[] stateParams)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = Mitch_HW.Command.CMD_STATE.code;
        if (state != Mitch_HW.SystemState.SYS_NULL)
        {
            // Header
            respLen = 1;
            buffer[1] = respLen;

            switch (state)
            {
                case SYS_BOOT_STARTUP:
                case SYS_BOOT_IDLE:
                case SYS_BOOT_WRITE:
                case SYS_ERROR:
                    // Do nothing!
                    break;
                case SYS_STARTUP:
                    buffer[2] = state.code;
                    break;
                case SYS_IDLE:
                    buffer[2] = state.code;
                    break;
                case SYS_STANDBY:
                    buffer[2] = state.code;
                    break;
                case SYS_LOG:
                    // Do nothing -> use the proper method!
                    break;
                case SYS_READOUT:
                    // Do nothing!
                    break;
                default:
                    break;
            }
            buffer[1] = respLen;
        }

        return buffer;
    }

    /// <summary>
    /// Start log command
    /// @param logMode log mode to be set.
    /// @param logFreq log frequency to be set.
    /// @return A byte array of 20 elements containing the encoded start log command and the necessary parameters to be sent.
    /// </summary>
    public static byte[] Cmd_StartLog(Mitch_HW.LogMode logMode, Mitch_HW.LogFrequency logFreq)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 3;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_STATE.code;       // command
        buffer[1] = respLen;                // length

        // Payload: nothing
        buffer[2] = Mitch_HW.SystemState.SYS_LOG.code;
        buffer[3] = logMode.code;
        buffer[4] = logFreq.code;

        return buffer;
    }

    /// <summary>
    /// Stop acquisition in log mode.
    /// @return A byte array of 20 elements containing the encoded stop log acquisition command to be sent.
    /// </summary>
    public static byte[] Cmd_StopLog()
    {
        // Build message buffer
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 1;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_STATE.code;       // command
        buffer[1] = respLen;                // length

        // Payload: nothing
        buffer[2] = Mitch_HW.SystemState.SYS_IDLE.code;

        return buffer;
    }

    /// <summary>
    /// Start acquisition in streaming mode
    /// @param streamMode stream mode to be set.
    /// @param streamFreq stream frequency to be set.
    /// @return A byte array of 20 elements containing the encoded start stream command and the necessary parameters to be sent.
    /// </summary>
    public static byte[] Cmd_StartStreaming(Mitch_HW.StreamMode streamMode, Mitch_HW.StreamFrequency streamFreq)
    {
        // Build message buffer
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 3;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_STATE.code;       // command
        buffer[1] = respLen;                // length

        // Payload: nothing
        buffer[2] = Mitch_HW.SystemState.SYS_TX.code;
        buffer[3] = streamMode.code;       
        buffer[4] = streamFreq.code;       // Force streaming @ 5Hz

        return buffer;
    }

    /// <summary>
    /// Stop acquisition in streaming mode
    /// @return A 20-byte array containing the encoded stop streaming command to be sent.
    /// </summary>
    public static byte[] Cmd_StopStreaming()
    {
        // Build message buffer
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 1;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_STATE.code;       // command
        buffer[1] = respLen;                // length

        // Payload: nothing
        buffer[2] = (byte)Mitch_HW.SystemState.SYS_IDLE.code;

        return buffer;
    }

    /// <summary>
    /// Restart device
    /// @param restartMode selected mode at which the device must be restarted.
    /// @return A 20-byte array containing the encoded restart command and required parameters to be sent.
    /// </summary>
    public static byte[] Cmd_Restart(Mitch_HW.RestartMode restartMode)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 1;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_RESTART.code;     // command
        buffer[1] = respLen;                // length

        // Payload
        buffer[2] = (byte)restartMode.code;

        return buffer;
    }

    /// <summary>
    /// Get Cyclic Redundancy Code value of the current firmware
    /// @return A 20-byte array containing the encoded get CRC command to be sent.
    /// </summary>
    public static byte[] Cmd_GetCRC()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = (byte)(Mitch_HW.Command.CMD_APP_CRC.code + 0x80);
        buffer[1] = respLen;

        return buffer;
    }

    // CMD_FW_UPLOAD = 0x05,           //!< CMD_FW_UPLOAD

    // CMD_START_APP = 0x06,           //!< CMD_START_APP

    /// <summary>
    /// Get Battery Charge level [%]
    /// @return A 20-byte array containing the encoded get battery charge command to be sent.
    /// </summary>
    public static byte[] Cmd_Battery_Charge()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = (byte)(Mitch_HW.Command.CMD_BATTERY_CHARGE.code + 0x80);
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Get Battery Voltage level [mV]
    /// @return A 20-byte array containing the encoded get battery voltage command to be sent.
    /// </summary>
    public static byte[] Cmd_Battery_Voltage()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = (byte)(Mitch_HW.Command.CMD_BATTERY_VOLTAGE.code + 0x80);
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Get Check up flag value
    /// @return A 20-byte array containing the encoded check up command to be sent.
    /// </summary>
    public static byte[] Cmd_Checkup()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        // Header
        buffer[0] = (byte)(Mitch_HW.Command.CMD_CHECK_UP.code + 0x80);
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Get current firmware version
    /// @return A 20-byte array containing the encoded get firmware version command to be sent.
    /// </summary>
    public static byte[] Cmd_FwVersion()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        // Header
        buffer[0] = (byte)(Mitch_HW.Command.CMD_FW_VERSION.code + 0x80);
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Get current date/time [epoch]
    /// @return A 20-byte array containing the encoded get date/time command to be sent.
    /// </summary>
    public static byte[] Cmd_GetTime()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = (byte)(Mitch_HW.Command.CMD_TIME.code + 0x80);      // get date
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Set current date/time [epoch]
    /// @return A 20-byte array containing the encoded set date/time command to be sent.
    /// </summary>
    public static byte[] Cmd_SetTime()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = Mitch_HW.Command.CMD_TIME.code;      // sset date
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Access to Bluetooth module name [get/set]
    /// @param enableRead flag to enable or disable the read/write mode
    /// @param bleName string containing the name to be set
    /// @return A 20-byte array containing the encoded get/set BLE name command to be sent.
    /// </summary>
    public static byte[] Cmd_BLE_Name(boolean enableRead, String bleName)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        if (enableRead)
        {
            // Header
            buffer[0] = (byte)(Mitch_HW.Command.CMD_BLE_NAME.code + 0x80);
            buffer[1] = respLen;
        }
        else
        {
            // Header
            buffer[0] = Mitch_HW.Command.CMD_BLE_NAME.code;
            respLen = (byte)bleName.length();
            buffer[1] = respLen;

            // Payload
            if (bleName != "" && respLen <= 15)
            {
                byte[] tmpName = bleName.getBytes();
                for (int i = 0; i < respLen; i++)
                    buffer[2 + i] = tmpName[i];
            }
        }

        return buffer;
    }

    // CMD_HW_VERSION = 0x0D,          //!< CMD_HW_VERSION
    // Not implemented!

    /// <summary>
    /// Access to memory state
    /// @param enableRead flag to enable or disable the read/write mode. If executed in write mode, the command is for erase memory.
    /// @return A 20-byte array containing the encoded get/set memroy control command to be sent.
    /// </summary>
    //// memory
    public static byte[] Cmd_Memory_Control(boolean enableRead)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        // Header
        if (enableRead)
            buffer[0] = (byte)(Mitch_HW.Command.CMD_MEM_CONTROL.code + 0x80);
        else
            buffer[0] = Mitch_HW.Command.CMD_MEM_CONTROL.code;
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Access to file info given a specific index
    /// @param fileId numeric identifier of the file to be check
    /// @return A 20-byte array containing the encoded get file info command to be sent.
    /// </summary>
    public static byte[] Cmd_Memory_FileInfo(int fileId)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 2;

        // Header
        buffer[0] = (byte)(Mitch_HW.Command.CMD_MEM_FILE_INFO.code + 0x80);
        buffer[1] = respLen;

        // Payload
        byte[] valueBytes = intToByteArray(fileId);
        buffer[2] = valueBytes[0];
        buffer[3] = valueBytes[1];

        return buffer;
    }

    /// <summary>
    /// File download
    /// #param fileId numeric identifier of the file to be downloaded
    /// @return A 20-byte array containing the encoded file download command to be sent.
    /// </summary>
    public static byte[] Cmd_Memory_FileDownload(int fileId)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 2;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_MEM_FILE_DOWNLOAD.code;
        buffer[1] = respLen;

        // Payload
        byte[] valueBytes = intToByteArray(fileId);
        buffer[3] = valueBytes[1];
        buffer[2] = valueBytes[0];

        return buffer;
    }

    // CMD_CLK_DRIFT = 0x30,           //!< CMD_CLK_DRIFT
    // Not implemented!

    /// <summary>
    /// Get Clock offset
    /// @return A 20-byte array containing the encoded get clock offset command to be sent.
    /// </summary>
    public static byte[] Cmd_GetClockOffset()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = (byte)(Mitch_HW.Command.CMD_CLK_OFFSET.code + 0x80);
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Set Clock offset
    /// @return A 20-byte array containing the encoded set clock offset command to be sent.
    /// </summary>
    public static byte[] Cmd_SetClockOffset(double inOffset)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = Mitch_HW.Command.CMD_CLK_OFFSET.code;
        respLen = 8;

        // Payload
        byte[] valueBytes = doubletoBytes(inOffset);
        buffer[9] = valueBytes[7];
        buffer[8] = valueBytes[6];
        buffer[7] = valueBytes[5];
        buffer[6] = valueBytes[4];
        buffer[5] = valueBytes[3];
        buffer[4] = valueBytes[2];
        buffer[3] = valueBytes[1];
        buffer[2] = valueBytes[0];

        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Enter timesync mode
    /// @return A 20-byte array containing the encoded enter timesync command to be sent.
    /// </summary>
    public static byte[] Cmd_EnterTimeSync(boolean enableRead)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        if (enableRead)
            buffer[0] = (byte)(Mitch_HW.Command.CMD_TIME_SYNC.code + 0x80);
        else
            buffer[0] = Mitch_HW.Command.CMD_TIME_SYNC.code;

        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Exit timesync mode
    /// @return A 20-byte array containing the encoded exit timesync command to be sent.
    /// </summary>
    public static byte[] Cmd_ExitTimeSync()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = Mitch_HW.Command.CMD_EXIT_TIME_SYNC.code;
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Estimate Clock offset
    /// @return A 20-byte array containing the encoded estimate clock offset command to be sent.
    /// </summary>
    public static byte[] Cmd_EstimateOffset()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        buffer[0] = Mitch_HW.Command.CMD_TIME_SYNC.code;
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Get full scales for IMU module
    /// @param enableRead flat to enable or disable read/write mode
    /// @param gyr_FS to set gyroscope full scale among the available setup
    /// @param axl_FS to set accelerometer full scale among the available setup
    /// @return A 20-byte array containing the encoded get/set full scale command to be sent.
    /// </summary>
    public static byte[] Cmd_GetFS6DOFS(boolean enableRead, Mitch_HW.Gyroscope_FS gyr_FS, Mitch_HW.Accelerometer_FS axl_FS)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        if (enableRead)

            buffer[0] = (byte)(Mitch_HW.Command.CMD_FS_AXL_GYRO.code + 0x80);
        else
        {
            if (gyr_FS != Mitch_HW.Gyroscope_FS.GYR_FS_NULL && axl_FS != Mitch_HW.Accelerometer_FS.AXL_FS_NULL)
            {
                buffer[0] = Mitch_HW.Command.CMD_FS_AXL_GYRO.code;
                respLen = 2;

                // Payload: nothing
                buffer[2] = axl_FS.code;
                buffer[3] = gyr_FS.code;
            }
            else
            {
                // By default perform read action if write is not possible
                buffer[0] = (byte)(Mitch_HW.Command.CMD_FS_AXL_GYRO.code + 0x80);
            }
        }
            
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Set accelerometer full scale
    /// @param axl_FS to set accelerometer full scale among the available setup
    /// @return A 20-byte array containing the encoded set accelerometer full scale command to be sent.
    /// </summary>
    public static byte[] Cmd_SetAxlFS(Mitch_HW.Accelerometer_FS axl_FS)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        if (axl_FS != Mitch_HW.Accelerometer_FS.AXL_FS_NULL)
        {
            buffer[0] = Mitch_HW.Command.CMD_FS_AXL.code;
            respLen = 1;
        }
        else
        {
            // By default perform read action if write is not possible
            buffer[0] = (byte)(Mitch_HW.Command.CMD_FS_AXL_GYRO.code + 0x80);
        }

        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = axl_FS.code;

        return buffer;
    }

    /// <summary>
    /// Set gyroscope full scale
    /// @param gyr_FS to set gyroscope full scale among the available setup
    /// @return A 20-byte array containing the encoded set gyroscoope full scale command to be sent.
    /// </summary>
    public static byte[] Cmd_SetGyrFS(Mitch_HW.Gyroscope_FS gyr_FS)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        if (gyr_FS != Mitch_HW.Gyroscope_FS.GYR_FS_NULL)
        {
            buffer[0] = Mitch_HW.Command.CMD_FS_GYRO.code;
            respLen = 1;
        }
        else
        {
            // By default perform read action if write is not possible
            buffer[0] = (byte)(Mitch_HW.Command.CMD_FS_AXL_GYRO.code + 0x80);
        }

        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = gyr_FS.code;

        return buffer;
    }

    // CMD_AUTO_START = 0x43,                          //!< CMD_CMD_AUTO_START
    // Not implemented!

    /// <summary>
    /// Retrieve Time Of Flight full scale
    /// @param idx of the time of flight peripheral (e.g., 1 or 2)
    /// @return A 20-byte array containing the encoded get time of flight full scale command to be sent.
    /// </summary>
    public static byte[] Cmd_GetTOFFS(byte idx)
    {
        // CMD_FS_DS1 = 0x44,                              //!< CMD_FS_DS1
        // CMD_FS_DS2 = 0x45,                              //!< CMD_FS_DS2

        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        // Header
        switch (idx)
        {
        case 0x00:
            buffer[0] = (byte)(Mitch_HW.Command.CMD_FS_DS1.code + 0x80);
            break;
        case 0x01:
            buffer[0] = (byte)(Mitch_HW.Command.CMD_FS_DS2.code + 0x80);
            break;
        default:
            break;
        }

        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Set Time Of Flight full scale
    /// @param tof_FS full scale value to be set
    /// @param idx of the time of flight peripheral (e.g., 1 or 2) of interest
    /// @return A 20-byte array containing the encoded set time of flight full scale command to be sent.
    /// </summary>
    public static byte[] Cmd_SetTOFFS(Mitch_HW.TOF_FS tof_FS, byte idx)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        if (tof_FS != Mitch_HW.TOF_FS.TOF_FS_NULL)
        {
            switch (idx)
            {
                case 0x00:
                    buffer[0] = Mitch_HW.Command.CMD_FS_DS1.code;
                    break;
                case 0x01:
                    buffer[0] = Mitch_HW.Command.CMD_FS_DS2.code;
                    break;
                default:
                    buffer[0] = Mitch_HW.Command.CMD_FS_DS1.code;
                    break;
            }
            respLen = 1;
        }
        else
        {
            // By default perform read action if write is not possible
            buffer[0] = (byte)(Mitch_HW.Command.CMD_FS_DS1.code + 0x80);
        }
        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = tof_FS.code;

        return buffer;
    }

    /// <summary>
    /// Retrieve Time Of Flight offset
    /// @param idx of the time of flight peripheral (e.g., 1 or 2)
    /// @return A 20-byte array containing the encoded get time of flight offset command to be sent.
    /// </summary>
    public static byte[] Cmd_GetTOFOffset(byte idx)
    {
        // CMD_OFFSET_DS1 = 0x46,                          //!< CMD_OFFSET_DS1
        // CMD_OFFSET_DS2 = 0x47,                          //!< CMD_OFFSET_DS2

        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        // Header
        switch (idx)
        {
            case 0x00:
                buffer[0] = (byte)(Mitch_HW.Command.CMD_OFFSET_DS1.code + 0x80);
                break;
            case 0x01:
                buffer[0] = (byte)(Mitch_HW.Command.CMD_OFFSET_DS2.code + 0x80);
                break;
            default:
                break;
        }

        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Set Time Of Flight offset
    /// @offset to be set
    /// @param idx of the time of flight peripheral (e.g., 1 or 2) of interest
    /// @return A 20-byte array containing the encoded set time of flight offset command to be sent.
    /// </summary>
    public static byte[] Cmd_SetTOFOffset(int offset, byte idx)
    {
        // CMD_OFFSET_DS1 = 0x46,                          //!< CMD_OFFSET_DS1
        // CMD_OFFSET_DS2 = 0x47,                          //!< CMD_OFFSET_DS2

        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        // Header
        switch (idx)
        {
            case 0x00:
                buffer[0] = Mitch_HW.Command.CMD_OFFSET_DS1.code;
                buffer[2] = (byte)offset;
                respLen = 1;
                break;
            case 0x01:
                buffer[0] = Mitch_HW.Command.CMD_OFFSET_DS2.code;
                buffer[2] = (byte)offset;
                respLen = 1;
                break;
            default:
                break;
        }

        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Retrieve unique device identifier
    /// @return A 20-byte array containing the encoded get device identifier command to be sent.
    /// </summary>
    public static byte[] Cmd_GetDeviceID()
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 0;

        // Header
        buffer[0] = (byte)(Mitch_HW.Command.CMD_DEVICE_ID.code + 0x80);
        buffer[1] = respLen;

        return buffer;
    }

    /// <summary>
    /// Retrieve accelerometer calibration matrix
    /// @param lineId identifier of the matrix line to be retrived
    /// @return A 20-byte array containing the encoded get accelerometer calibration matrix command to be sent.
    /// </summary>
    public static byte[] Cmd_GetAxlCalibration(byte lineId)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 2;

        // Header
        buffer[0] = (byte)(Mitch_HW.Command.CMD_MATRIX_CALIBRATION.code + 0x80);
        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = Mitch_HW.CalibMatrixType.AXL_MATRIX.code;      // Matrix type
        if (lineId >= 1 && lineId <= 3)                             // Line
            buffer[3] = lineId;

        return buffer;
    }

    /// <summary>
    /// Set accelerometer calibration matrix
    /// @param lineId identifier of the matrix line to be set
    /// @param val1 first value (cell indexes lineId,1)
    /// @param val2 second value (cell indexes lineId,2)
    /// @param val3 third value (cell indexes lineId,3)
    /// @param val4 fourth (offset) value (cell indexes lineId,4)
    /// @return A 20-byte array containing the encoded set acceleroometer calibration matrix command to be sent.
    /// </summary>
    public static byte[] Cmd_SetAxlCalibration(byte lineId, float val1, float val2, float val3, float val4)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 18;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_MATRIX_CALIBRATION.code;
        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = Mitch_HW.CalibMatrixType.AXL_MATRIX.code;      // Matrix type
        // if (lineId >= 0 && lineId <= 2)
        // {
        buffer[3] = lineId; // Line
        byte[] valByteArray = floatToByteArray(val1);
        System.arraycopy(valByteArray, 0, buffer, 4,  4);  // Value 1
        valByteArray = floatToByteArray(val2);
        System.arraycopy(valByteArray, 0, buffer, 8,  4);  // Value 2
        valByteArray = floatToByteArray(val3);
        System.arraycopy(valByteArray, 0, buffer, 12, 4);  // Value 3
        valByteArray = floatToByteArray(val4);
        System.arraycopy(valByteArray, 0, buffer, 16, 4);  // Value 4 (offset)
        // }

        return buffer;
    }

    /// <summary>
    /// Retrieve gyroscope calibration matrix
    /// @param lineId identifier of the matrix line to be retrived
    /// @return A 20-byte array containing the encoded get gyroscope calibration matrix command to be sent.
    /// </summary>
    public static byte[] Cmd_GetGyrCalibration(byte lineId)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 2;

        // Header
        buffer[0] = (byte)(Mitch_HW.Command.CMD_MATRIX_CALIBRATION.code + 0x80);
        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = Mitch_HW.CalibMatrixType.GYR_MATRIX.code;      // Matrix type
        if (lineId >= 1 && lineId <= 3)                             // Line
            buffer[3] = lineId;

        return buffer;
    }

    /// <summary>
    /// Set gyroscope calibration matrix
    /// @param lineId identifier of the matrix line to be set
    /// @param val1 first value (cell indexes lineId,1)
    /// @param val2 second value (cell indexes lineId,2)
    /// @param val3 third value (cell indexes lineId,3)
    /// @param val4 fourth (offset) value (cell indexes lineId,4)
    /// @return A 20-byte array containing the encoded set gyroscope calibration matrix command to be sent.
    /// </summary>
    public static byte[] Cmd_SetGyrCalibration(byte lineId, float val1, float val2, float val3, float val4)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 18;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_MATRIX_CALIBRATION.code;
        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = Mitch_HW.CalibMatrixType.GYR_MATRIX.code;      // Matrix type
        // if (lineId >= 0 && lineId <= 2)
        // {
        buffer[3] = lineId; // Line
        byte[] valByteArray = floatToByteArray(val1);
        System.arraycopy(valByteArray, 0, buffer, 4, 4);  // Value 1
        valByteArray = floatToByteArray(val2);
        System.arraycopy(valByteArray, 0, buffer, 8, 4);  // Value 2
        valByteArray = floatToByteArray(val3);
        System.arraycopy(valByteArray, 0, buffer, 12, 4);  // Value 3
        valByteArray = floatToByteArray(val4);
        System.arraycopy(valByteArray, 0, buffer, 16, 4);  // Value 4 (offset)
        // }

        return buffer;
    }

    /// <summary>
    /// Retrieve magnetometer calibration matrix
    /// @param lineId identifier of the matrix line to be retrived
    /// @return A 20-byte array containing the encoded get magnetometer calibration matrix command to be sent.
    /// </summary>
    public static byte[] Cmd_GetMagCalibration(byte lineId)
    {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 2;

        // Header
        buffer[0] = (byte)(Mitch_HW.Command.CMD_MATRIX_CALIBRATION.code + 0x80);
        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = Mitch_HW.CalibMatrixType.MAG_MATRIX.code;      // Matrix type
        if (lineId >= 1 && lineId <= 3)                             // Line
            buffer[3] = lineId;

        return buffer;
    }

    /// <summary>
    /// Set magnetometer calibration matrix
    /// @param lineId identifier of the matrix line to be set
    /// @param val1 first value (cell indexes lineId,1)
    /// @param val2 second value (cell indexes lineId,2)
    /// @param val3 third value (cell indexes lineId,3)
    /// @param val4 fourth (offset) value (cell indexes lineId,4)
    /// @return A 20-byte array containing the encoded set magnetometer calibration matrix command to be sent.
    /// </summary>
    public static byte[] Cmd_SetMagCalibration(byte lineId, float val1, float val2, float val3, float val4) {
        byte[] buffer = new byte[Mitch_HW.COMM_MESSAGE_LEN];
        byte respLen = 18;

        // Header
        buffer[0] = Mitch_HW.Command.CMD_MATRIX_CALIBRATION.code;
        buffer[1] = respLen;

        // Payload: nothing
        buffer[2] = Mitch_HW.CalibMatrixType.MAG_MATRIX.code;      // Matrix type
        // if (lineId >= 0 && lineId <= 2)
        // {
        buffer[3] = lineId; // Line
        byte[] valByteArray = floatToByteArray(val1);
        System.arraycopy(valByteArray, 0, buffer, 4, 4);  // Value 1
        valByteArray = floatToByteArray(val2);
        System.arraycopy(valByteArray, 0, buffer, 8, 4);  // Value 2
        valByteArray = floatToByteArray(val3);
        System.arraycopy(valByteArray, 0, buffer, 12, 4);  // Value 3
        valByteArray = floatToByteArray(val4);
        System.arraycopy(valByteArray, 0, buffer, 16, 4);  // Value 4 (offset)
        // }

        return buffer;
    }
    // endregion
}

