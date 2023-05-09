public class Mitch_HW {
    /// <summary>Overall message length [Bytes]</summary>
    public static final int COMM_MESSAGE_LEN = 20;

    /// <summary>Message Header of 4 Bytes length</summary>
    public static final int COMM_MSG_HEADER_LEN_4 = 4;
    /// <summary>Message Header of 2 Bytes length</summary>
    public static final int COMM_MSG_HEADER_LEN_2 = 2;

    /// <summary>Command, Payload maximum length [Bytes]</summary>
    public static final int COMM_PAYLOAD_LEN_18 = 18;
    /// <summary>Response, Payload maximum length [Bytes]</summary>
    public static final int COMM_PAYLOAD_LEN_16 = 16;

    /// <summary>Number of data channels (9 Degrees of Freedom, e.g. Gyr+Axl+Mag)</summary>
    public static final int NUM_OF_CHANNELS_9DOF = 3;
    /// <summary>Number of data channels (Time Of Flight)</summary>
    public static final int NUM_OF_CHANNELS_TOF = 2;

    /// <summary>
    /// Command statements
    /// </summary>
    public static enum Command {
        /// <summary>Acknowledge</summary>
        CMD_ACK((byte)0x00),
        /// <summary>Shutdown</summary>
        CMD_SHUTDOWN((byte)0x01),
        /// <summary>State [get/set]</summary>
        CMD_STATE((byte)0x02),
        /// <summary>Restart</summary>
        CMD_RESTART((byte)0x03),
        /// <summary>Cyclical Redundancy Checking [readonly]</summary>
        CMD_APP_CRC((byte)0x04),
        /// <summary>Firmware Upload [<b>bootloader mode only</b>]</summary>
        CMD_FW_UPLOAD((byte)0x05),
        /// <summary>Start Application</summary>
        CMD_START_APP((byte)0x06),
        /// <summary>Battery Charge [readonly]</summary>
        CMD_BATTERY_CHARGE((byte)0x07),
        /// <summary>Battery Voltage [readonly]</summary>
        CMD_BATTERY_VOLTAGE((byte)0x08),
        /// <summary>Device check up flag [readonly]</summary>
        CMD_CHECK_UP((byte)0x09),
        /// <summary>Installed Firmware Version [readonly]</summary>
        CMD_FW_VERSION((byte)0x0A),
        /// <summary>Current Time [get/set]</summary>
        CMD_TIME((byte)0x0B),
        /// <summary>Bluetooth Module Name [get/set]</summary>
        CMD_BLE_NAME((byte)0x0C),
        /// <summary>Hardware version [readonly]</summary>
        CMD_HW_VERSION((byte)0x0D),
        /// <summary>Device Identification code [readonly]</summary>
        CMD_DEVICE_ID((byte)0x0E),

        /// <summary>Memory state [readonly]</summary>
        CMD_MEM_CONTROL((byte)0x20),
        /// <summary>Get file information [readonly]</summary>
        CMD_MEM_FILE_INFO((byte)0x21),
        /// <summary>File download</summary>
        CMD_MEM_FILE_DOWNLOAD((byte)0x22),

        /// <summary>Clock drift [get/set]</summary>
        CMD_CLK_DRIFT((byte)0x30),
        /// <summary>Clock offset [get/set]</summary>
        CMD_CLK_OFFSET((byte)0x31),
        /// <summary>Enter time sync mode</summary>
        CMD_TIME_SYNC((byte)0x32),
        /// <summary>Exit time sync mode</summary>
        CMD_EXIT_TIME_SYNC((byte)0x33),

        /// <summary>Accelerometer + Gyroscope full scales [get/set]</summary>
        CMD_FS_AXL_GYRO((byte)0x40),
        /// <summary>Accelerometer full scale [get/set]</summary>
        CMD_FS_AXL((byte)0x41),
        /// <summary>Gyroscope full scale [get/set]</summary>
        CMD_FS_GYRO((byte)0x42),

        /// <summary>Automatic start</summary>
        CMD_AUTO_START((byte)0x43),

        /// <summary>Full scale of TOF#1 [get/set]</summary>
        CMD_FS_DS1((byte)0x44),
        /// <summary>Full scale of TOF#2 [get/set]</summary>
        CMD_FS_DS2((byte)0x45),
        /// <summary>Offset of TOF#1 [get/set]</summary>
        CMD_OFFSET_DS1((byte)0x46),
        /// <summary>Offset of TOF#2 [get/set]</summary>
        CMD_OFFSET_DS2((byte)0x47),

        /// <summary>Device calibration matrices [get/set]</summary>
        CMD_MATRIX_CALIBRATION((byte)0x48),

        /// <summary>Log button</summary>
        CMD_BTN_LOG((byte)0x50); 
    
        public final byte code;

        private Command(byte code) {
            this.code=code;
        }
    };

    /// <summary>
    /// Acknowledge types
    /// </summary>
    public static enum Acknowledge_Type {
        /// <summary>Positive Acknowledge</summary>
        CMD_ACK_SUCCESS((byte)0x00),
        /// <summary>Negative Acknowledge</summary>
        CMD_ACK_ERROR((byte)0x01);
        
        public final byte code;

        private Acknowledge_Type(byte code) {
            this.code=code;
        }
    }

    /// <summary>
    /// System States
    /// </summary>
    public static enum SystemState {
        /// <summary>System state NULL</summary>
        SYS_NULL((byte)0x00),
        /// <summary>Startup state [<b>bootloader mode only</b>]</summary>
        SYS_BOOT_STARTUP((byte)0xF0),
        /// <summary>Idle state [<b>bootloader mode only</b>]</summary>
        SYS_BOOT_IDLE((byte)0xF1),
        /// <summary>Write state [<b>bootloader mode only</b>]</summary>
        SYS_BOOT_WRITE((byte)0xF2),
        /// <summary>Error state</summary>
        SYS_ERROR((byte)0xFF),
        /// <summary>Startup state</summary>
        SYS_STARTUP((byte)0x01),
        /// <summary>Idle state</summary>
        SYS_IDLE((byte)0x02),
        /// <summary>Standby</summary>
        SYS_STANDBY((byte)0x03),
        /// <summary>Log state</summary>
        SYS_LOG((byte)0x04),                                 
        /// <summary>Readout memory state</summary>
        SYS_READOUT((byte)0x05),
        /// <summary>Streaming state</summary>
        SYS_TX((byte)0xF8);
        
        public final byte code;

        private SystemState(byte code) {
            this.code=code;
        }
    };

    /// <summary>
    /// Restart modes
    /// </summary>
    public enum RestartMode {
        /// <summary>Restart device in application mode</summary>
        RESTART_RESET((byte)0x00),
        /// <summary>Restart device in bootloader mode</summary>
        RESTART_BOOT_LOADER((byte)0x01);     

        public final byte code;

        private RestartMode(byte code) {
            this.code=code;
        }   
    };

    /// <summary>
    /// Allowed acquisition types, operating in log mode
    /// </summary>
    public static enum LogMode {
        /// <summary>None</summary>
        LOG_MODE_NONE((byte)0x00),
        /// <summary>Inertial Measurement Unit</summary>
        LOG_MODE_IMU((byte)0x01),
        /// <summary>Inertial Measurement Unit + Pressure / Insole</summary>
        LOG_MODE_IMU_INSOLE((byte)0x02),
        /// <summary>All</summary>
        LOG_MODE_ALL((byte)0x03),
        /// <summary>Inertial Measurement Unit + Timestamp</summary>
        LOG_MODE_IMU_TIMESTAMP((byte)0x04),
        /// <summary>Inertial Measurement Unit + Pressure / Insole + Timestamp</summary>
        LOG_MODE_IMU_INSOLE_TIMPESTAMP((byte)0x05),
        /// <summary>All + Timestamp</summary>
        LOG_MODE_ALL_TIMESTAMP((byte)0x06);
    
        public final byte code;

        private LogMode(byte code) {
            this.code=code;
        }
    };

    /// <summary>
    /// Allowed acquisition frequencies, operating in log mode
    /// </summary>
    public enum LogFrequency {
        /// <summary>None</summary>
        LOG_FREQ_NONE((byte)0x00),
        /// <summary>25 Hz</summary>
        LOG_FREQ_25HZ((byte)0x01),
        /// <summary>50 Hz</summary>
        LOG_FREQ_50HZ((byte)0x02),
        /// <summary>100 Hz</summary>
        LOG_FREQ_100HZ((byte)0x04),
        /// <summary>200 Hz</summary>
        LOG_FREQ_200HZ((byte)0x08),
        /// <summary>500 Hz</summary>
        LOG_FREQ_500HZ((byte)0x14),
        /// <summary>1000 Hz</summary>
        LOG_FREQ_1000HZ((byte)0x28);  

        public final byte code;

        private LogFrequency(byte code) {
            this.code=code;
        }
    };

    /// <summary>
    /// Packet dimensions (log)
    /// </summary>
    public enum LogPacketDimension {
        /// <summary>None</summary>
        LOG_PACKET_DIM_NONE((byte)0),
        /// <summary>Inertial Measurement Unit</summary>
        LOG_PACKET_DIM_IMU((byte)18),
        /// <summary>Inertial Measurement Unit + Pressure / Insole</summary>
        LOG_PACKET_DIM_IMU_INSOLE((byte)42),
        /// <summary>All</summary>
        LOG_PACKET_DIM_ALL((byte)42),
        /// <summary>Inertial Measurement Unit + Timestamp</summary>
        LOG_PACKET_DIM_IMU_withTimestamp((byte)26),
        /// <summary>Inertial Measurement Unit + Pressure / Insole + Timestamp</summary>
        LOG_PACKET_DIM_IMU_INSOLE_withTimestamp((byte)50),
        /// <summary>All + Timestamp</summary>
        LOG_PACKET_DIM_ALL_withTimestamp((byte)52);    
 
        public final byte code;

        private LogPacketDimension(byte code) {
            this.code=code;
        }   
    };

    /// <summary>
    /// Allowed acquisition types, operating in streaming mode
    /// </summary>
    public static enum StreamMode {
        /// <summary>None</summary>
        STREAM_MODE_NONE((byte)0x00),
        /// <summary>Pressure</summary>
        STREAM_MODE_PRESSURE((byte)0x01),
        /// <summary>Gyroscope + Accelerometer + Time Of Flight</summary>
        STREAM_MODE_6DOF_TOF((byte)0x02),
        /// <summary>Time Of Flight</summary>
        STREAM_MODE_TOF((byte)0x03),
        /// <summary>Gyroscope + Accelerometer</summary>
        STREAM_MODE_6DOF((byte)0x04),
        /// <summary>Gyroscope + Accelerometer + Magnetometer</summary>
        STREAM_MODE_9DOF((byte)0x05),
        /// <summary>Gyroscope + Accelerometer + Quaternion</summary>
        STREAM_MODE_6DOFs_ORIENTATION((byte)0x06),
        /// <summary>Quaternion</summary>
        STREAM_MODE_ORIENTATION((byte)0x07);
 
        public final byte code;

        private StreamMode(byte code) {
            this.code=code;
        }     
    };

    /// <summary>
    /// Allowed acquisition frequencies, operating in streaming mode
    /// </summary>
    public static enum StreamFrequency {
        /// <summary>None</summary>
        STREAM_FREQ_NONE((byte)0x00),
        /// <summary>5 Hz</summary>
        STREAM_FREQ_5Hz((byte)0x01),
        /// <summary>10 Hz</summary>
        STREAM_FREQ_10Hz((byte)0x02),
        /// <summary>25 Hz</summary>
        STREAM_FREQ_25Hz((byte)0x03),
        /// <summary>50 Hz</summary>
        STREAM_FREQ_50Hz((byte)0x04);
 
        public final byte code;

        private StreamFrequency(byte code) {
            this.code=code;
        }    
    };

    /// <summary>
    /// Packet dimensions (stream)
    /// </summary>
    public static enum StreamPacketDimension {
        /// <summary>None</summary>
        STREAM_PACKET_DIM_NONE((byte)0),
        /// <summary>Pressure</summary>
        STREAM_PACKET_DIM_PRESSURE((byte)18),
        /// <summary>Gyroscope + Accelerometer + Time Of Flight</summary>
        STREAM_PACKET_DIM_6DOF_TOF((byte)16),
        /// <summary>Time Of Flight</summary>
        STREAM_PACKET_DIM_TOF((byte)4),
        /// <summary>Gyroscope + Accelerometer</summary>
        STREAM_PACKET_DIM_6DOF((byte)14),
        /// <summary>Gyroscope + Accelerometer + Magnetometer</summary>
        STREAM_PACKET_DIM_9DOF((byte)18);      
 
        public final byte code;

        private StreamPacketDimension(byte code) {
            this.code=code;
        }   
    };

    /// <summary>
    /// Memory slot selector
    /// </summary>
    public static enum MemorySelection {
        /// <summary>Memory slot selector #1</summary>
        MEMORY_ONE((byte)0x01),
        /// <summary>Memory slot selector #2</summary>
        MEMORY_TWO((byte)0x02);                               
    
        public final byte code;

        private MemorySelection(byte code) {
            this.code=code;
        }     
    };

    /// <summary>
    /// Memory erase type
    /// </summary>
    public enum MemoryErase_Type {
        /// <summary>Memory slot selector #1</summary>
        ERASE_NONE((byte)0x00),
        /// <summary>Partial erase of the flash memory, from the beginning to the last sector occupied</summary>
        ERASE_PARTIAL((byte)0x01),
        /// <summary>Total erase of the memory slot #1</summary>
        ERASE_BULK1((byte)0x02),                           
        /// <summary>Total erase of the memory slot #2</summary>
        ERASE_BULK2((byte)0x03);                              
         
        public final byte code;

        private MemoryErase_Type(byte code) {
            this.code=code;
        }    
    };
   

    /// <summary>
    /// Gyroscope full scale values [dps]
    /// </summary>
    public enum Gyroscope_FS {
        /// <summary>None</summary>
        GYR_FS_NULL((byte)0xFF),
        /// <summary>245 dps</summary>
        GYR_FS_245_DPS((byte)0x00),
        /// <summary>500 dps</summary>
        GYR_FS_500_DPS((byte)0x04),
        /// <summary>1000 dps</summary>
        GYR_FS_1000_DPS((byte)0x08),
        /// <summary>2000 dps</summary>
        GYR_FS_2000_DPS((byte)0x0C);
    
        public final byte code;

        private Gyroscope_FS(byte code) {
            this.code=code;
        }    
    };


    /// <summary>Gyroscope resolution with a FS of 245 dps</summary>
    public final float GYR_RESOLUTION_245dps = 8.75f;
    /// <summary>Gyroscope resolution with a FS of 500 dsp </summary>
    public final float GYR_RESOLUTION_500dps = 17.5f;
    /// <summary>Gyroscope resolution with a FS of  1000 dps </summary>
    public final float GYR_RESOLUTION_1000dps = 35f;
    /// <summary>Gyroscope resolution with a FS of  2000 dps </summary>
    public final float GYR_RESOLUTION_2000dps = 70f;

    /// <summary> 
    /// Accelerometer full scale values [g]
    /// </summary>
    public enum Accelerometer_FS {
        /// <summary>None</summary>
        AXL_FS_NULL((byte)0xFF),
        /// <summary>2 g</summary>
        AXL_FS_2_g((byte)0x00),
        /// <summary>4 g </summary>
        AXL_FS_4_g((byte)0x08),
        /// <summary>8 g </summary>
        AXL_FS_8_g((byte)0x0C),
        /// <summary>16 g </summary>
        AXL_FS_16_g((byte)0x04);
    
        public final byte code;

        private Accelerometer_FS(byte code) {
            this.code=code;
        }    
    };


    /// <summary>Accelerometer resolution with a FS of 2g</summary>
    public final float AXL_RESOLUTION_2g = 0.061f;
    /// <summary>Accelerometer resolution with a FS of 4g</summary>
    public final float AXL_RESOLUTION_4g = 0.122f;
    /// <summary>Accelerometer resolution with a FS of 8g</summary>
    public final float AXL_RESOLUTION_8g = 0.244f;
    /// <summary>Accelerometer resolution with a FS of 16g</summary>
    public final float AXL_RESOLUTION_16g = 0.488f;

    /// <summary>Magnetometer resolution</summary>
    public final float MAG_RESOLUTION = 1.5f;

    /// <summary> 
    /// Time Of Flight distance sensor full scales
    /// </summary>
    public enum TOF_FS {
        /// <summary>None</summary>
        TOF_FS_NULL((byte)0x00),
        /// <summary>200 mm</summary>
        TOF_FS_200mm((byte)0x01),                           
        /// <summary>400 mm</summary>
        TOF_FS_400mm((byte)0x02),                           
        /// <summary>600 mm</summary>
        TOF_FS_600mm((byte)0x03);                            
    
        public final byte code;

        private TOF_FS(byte code) {
            this.code=code;
        }    
    };


    /// <summary> 
    /// Calibration matrices index
    /// </summary>
    public enum CalibMatrixType {
        /// <summary>Accelerometer</summary>
        AXL_MATRIX((byte)0x00),
        /// <summary>Gyroscope</summary>
        GYR_MATRIX((byte)0x01),
        /// <summary>Magnetometer</summary>
        MAG_MATRIX((byte)0x02);
    
        public final byte code;

        private CalibMatrixType(byte code) {
            this.code=code;
        }     
    };

}