package com.netease.jv.cmpp.bean;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netease.jv.cmpp.NettyClient;

@Data
public class CMPPMessage {

    /**
     * The constant PROTOCOL_HEAD_LENGTH.
     */
    public static final short PROTOCOL_HEAD_LENGTH = 12;
    private static final Logger loggerException = LoggerFactory.getLogger(CMPPMessage.class);
    private final int totalLength;
    private final Command command;
    private final int serial;
    private final byte[] content;

    /**
     * Instantiates a new Bili bili message.
     *
     * @param command the command
     * @param content the content
     */
    public CMPPMessage(Command command, byte[] content) {
        this.command = command;
        this.serial = NettyClient.getSerial();
        this.content = content;
        this.totalLength = PROTOCOL_HEAD_LENGTH + content.length;
    }

    public CMPPMessage(int totalLength, Command command, int serial, byte[] content) {
        this.totalLength = totalLength;
        this.command = command;
        this.serial = serial;
        this.content = content;
    }

    @Override
    public String toString() {
        return "CMPPMessage{" +
                "command=" + command +
                ", serial=" + serial +
                '}';
    }

    /**
     * The enum Action.
     */
    public enum Command {
        /**
         * 服务器返回当前直播间人数
         */
        CMPP_CONNECT(0x00000001),
        /**
         * 正常的服务器消息
         */
        CMPP_CONNECT_RESP(0x80000001),

        /**
         * Cmpp terminate action.
         */
        CMPP_TERMINATE(0x00000002),

        /**
         * Cmpp terminate response action.
         */
        CMPP_TERMINATE_RESPONSE(0x8000002),

        /**
         * Cmpp submit action.
         */
        CMPP_SUBMIT(0x00000004),

        /**
         * Cmpp submit response action.
         */
        CMPP_SUBMIT_RESPONSE(0x80000004),

        /**
         * Cmpp deliver action.
         */
        CMPP_DELIVER(0x00000005),

        /**
         * Cmpp deliver response action.
         */
        CMPP_DELIVER_RESPONSE(0x8000005),

        /**
         * Cmpp query action.
         */
        CMPP_QUERY(0x00000006),

        /**
         * Cmpp query response action.
         */
        CMPP_QUERY_RESPONSE(0x8000006),

        /**
         * Cmpp cancel action.
         */
        CMPP_CANCEL(0x00000007),

        /**
         * Cmpp cancel response action.
         */
        CMPP_CANCEL_RESPONSE(0x80000007),

        /**
         * Cmpp active test action.
         */
        CMPP_ACTIVE_TEST(0x00000008),

        /**
         * Cmpp active test response action.
         */
        CMPP_ACTIVE_TEST_RESPONSE(0x8000008);

        private final int value;

        Command(int value) {
            this.value = value;
        }

        /**
         * Value of action.
         *
         * @param value the value
         * @return the action
         */
        public static Command valueOf(int value) throws NoSuchCommandException {
            for (Command command : Command.values()) {
                if (command.value == value) {
                    return command;
                }
            }
            loggerException.error("invalid action : {} ", value);
            throw new NoSuchCommandException(value);
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public int getValue() {
            return value;
        }

        private static class NoSuchCommandException extends Exception {

            int command;

            public NoSuchCommandException(int command) {
                super("invalid command value " + command);
                this.command = command;
            }
        }
    }
}
