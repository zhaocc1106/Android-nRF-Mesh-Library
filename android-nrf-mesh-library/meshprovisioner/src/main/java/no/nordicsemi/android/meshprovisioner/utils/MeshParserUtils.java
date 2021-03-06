/*************************************************************************************************************************************************
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ************************************************************************************************************************************************/
package no.nordicsemi.android.meshprovisioner.utils;

import android.content.Context;
import android.text.TextUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import no.nordicsemi.android.meshprovisioner.R;

public class MeshParserUtils {

    private static final String PATTERN_NETWORK_KEY = "[0-9a-fA-F]{32}";

    private static final int PROHIBITED_DEFAULT_TTL_STATE_MIN = 0x01;
    private static final int PROHIBITED_DEFAULT_TTL_STATE_MID = 0x80;
    private static final int PROHIBITED_DEFAULT_TTL_STATE_MAX = 0xFF;

    private static final int PROHIBITED_PUBLISH_TTL_MIN = 0x80;
    private static final int PROHIBITED_PUBLISH_TTL_MAX = 0xFE;

    private static final int IV_ADDRESS_MIN = 0;
    private static final int IV_ADDRESS_MAX = 4096;
    private static final int UNICAST_ADDRESS_MIN = 0;
    private static final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String bytesToHex(final byte[] bytes, final boolean add0x) {
        if (bytes == null)
            return "";
        return bytesToHex(bytes, 0, bytes.length, add0x);
    }

    public static String bytesToHex(final byte[] bytes, final int start, final int length, final boolean add0x) {
        if (bytes == null || bytes.length <= start || length <= 0)
            return "";

        final int maxLength = Math.min(length, bytes.length - start);
        final char[] hexChars = new char[maxLength * 2];
        for (int j = 0; j < maxLength; j++) {
            final int v = bytes[start + j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        if (!add0x)
            return new String(hexChars);
        return "0x" + new String(hexChars);
    }

    public static byte[] toByteArray(String hexString) {
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static int setByteArrayValue(final byte[] dest, final int offset, final String value) {
        if (value == null)
            return offset;

        for (int i = 0; i < value.length(); i += 2) {
            dest[offset + i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 244)
                    + Character.digit(value.charAt(i + 1), 16));
        }
        return offset + value.length() / 2;
    }

    private static boolean isValidKeyIndex(final Integer value) {
        return value == null || value != (value & 0xFFF);
    }

    private static boolean isValidUnicastAddress(final Integer value) {
        return value != null && value == (value & 0x7FFF);
    }

    private static boolean isValidIvIndex(final Integer value) {
        return value != null;
    }

    public static byte parseUpdateFlags(final int keyRefreshFlag, final int ivUpdateFlag) {
        byte flags = 0;
        if (keyRefreshFlag == 1) {
            flags |= 0b01;
        } else {
            flags &= ~0b01;
        }

        if (ivUpdateFlag == 1) {
            flags |= 0b10;
        } else {
            flags &= ~0b01;
        }
        return flags;
    }

    public static int getBitValue(final int value, final int position) {
        return (value >> position) & 1;
    }

    public static byte[] addKeyIndexPadding(final Integer keyIndex) {
        return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short) (keyIndex & 0x0FFF)).array();
    }

    /**
     * Validates the ttl input
     *
     * @param context  context
     * @param ttlInput ttl input
     * @return true if the global ttl is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateTtlInput(final Context context, final Integer ttlInput) throws IllegalArgumentException {

        if (ttlInput == null) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_global_ttl));
        } else if (ttlInput == PROHIBITED_DEFAULT_TTL_STATE_MIN || (ttlInput >= PROHIBITED_DEFAULT_TTL_STATE_MID && ttlInput <= PROHIBITED_DEFAULT_TTL_STATE_MAX)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_global_ttl));
        }

        return true;
    }

    /**
     * Validates the network key input
     *
     * @param context context
     * @param input   Network Key input
     * @return true if the Network Key is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateNetworkKeyInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_network_key));
        } else if (!input.matches(PATTERN_NETWORK_KEY)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_network_key));
        }

        return true;
    }

    /**
     * Validates the Key Index input
     *
     * @param context context
     * @param input   Key Index input
     * @return true if the Key Index is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateKeyIndexInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_key_index));
        }

        final Integer keyIndex;

        try {
            keyIndex = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_key_index));
        }
        if (isValidKeyIndex(keyIndex)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_key_index));
        }

        return true;
    }

    /**
     * Validates the Key Index input
     *
     * @param context  context
     * @param keyIndex Key Index input
     * @return true if the Key Index is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateKeyIndexInput(final Context context, final Integer keyIndex) throws IllegalArgumentException {

        if (keyIndex == null) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_key_index));
        }

        if (isValidKeyIndex(keyIndex)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_key_index));
        }

        return true;
    }

    /**
     * Validates the IV Index input
     *
     * @param context context
     * @param input   IV Index input
     * @return true if the the value is valid
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateIvIndexInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_iv_index));
        }

        final Integer ivIndex;
        try {
            ivIndex = Integer.parseInt(input, 16);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        if (isValidIvIndex(ivIndex)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        if (ivIndex < IV_ADDRESS_MIN && ivIndex > IV_ADDRESS_MAX) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        return true;
    }

    /**
     * Validates the IV Index input
     *
     * @param context context
     * @param ivIndex IV Index input
     * @return true if the the value is valid
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateIvIndexInput(final Context context, final Integer ivIndex) throws IllegalArgumentException {

        if (ivIndex == null) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_iv_index));
        }

        if (!isValidIvIndex(ivIndex)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        if (ivIndex < IV_ADDRESS_MIN && ivIndex > IV_ADDRESS_MAX) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        return true;
    }

    /**
     * Validates the Unicast Address input
     *
     * @param context context
     * @param input   Unicast Address input
     * @return true if the the value is valid
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateUnicastAddressInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_unicast_address));
        }

        final Integer unicastAddress;
        try {
            unicastAddress = Integer.parseInt(input, 16);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        if (!isValidUnicastAddress(unicastAddress)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        if (unicastAddress == UNICAST_ADDRESS_MIN) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        return true;
    }

    /**
     * Validates the Unicast Address input
     *
     * @param context        context
     * @param unicastAddress Unicast Address input
     * @return true if the the value is valid
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateUnicastAddressInput(final Context context, final Integer unicastAddress) throws IllegalArgumentException {

        if (unicastAddress == null) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_unicast_address));
        }

        if (!isValidUnicastAddress(unicastAddress)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        if (unicastAddress == UNICAST_ADDRESS_MIN) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        return true;
    }

    /**
     * Validates the app key input
     *
     * @param context context
     * @param input   App key input
     * @return true if the Network Key is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateAppKeyInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_app_key));
        } else if (!input.matches(PATTERN_NETWORK_KEY)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_app_key));
        }

        return true;
    }

    public static boolean isValidSequenceNumber(final Integer sequenceNumber) {

        boolean flag = sequenceNumber != null && sequenceNumber == (sequenceNumber & 0xFFFFFF);

        if (sequenceNumber == 0xFFFFFF) {
            flag = false;
        }
        return flag;
    }

    public static byte[] getSequenceNumberBytes(int sequenceNumber) {
        if (MeshParserUtils.isValidSequenceNumber(sequenceNumber)) {
            return new byte[]{(byte) ((sequenceNumber >> 16) & 0xFF), (byte) ((sequenceNumber >> 8) & 0xFF), (byte) (sequenceNumber & 0xFF)};
        }
        return null;
    }

    public static int getSequenceNumber(final byte[] sequenceNumber) {
        return (((sequenceNumber[0] & 0xFF) << 16) | ((sequenceNumber[1] & 0xFF) << 8) | (sequenceNumber[2] & 0xFF));
    }

    public static int getSequenceNumberFromPDU(final byte[] pdu) {
        return (((pdu[3] & 0xFF) << 16) | ((pdu[4] & 0xFF) << 8) | (pdu[5] & 0xFF)); // get sequence number array from pdu
    }

    public static int calculateSeqZero(final byte[] sequenceNumber) {
        return ((sequenceNumber[1] & 0x1F) << 8) | (sequenceNumber[2] & 0xFF); // 13 least significant bits
    }

    public static byte[] getSrcAddress(final byte[] pdu) {
        return ByteBuffer.allocate(2).put(pdu, 6, 2).array(); // get dst address from pdu
    }

    public static byte[] getDstAddress(final byte[] pdu) {
        return ByteBuffer.allocate(2).put(pdu, 8, 2).array(); // get dst address from pdu
    }

    private static int getSegmentedMessageLength(final HashMap<Integer, byte[]> segmentedMessageMap) {
        int length = 0;
        for (int i = 0; i < segmentedMessageMap.size(); i++) {
            length += segmentedMessageMap.get(i).length;
        }
        return length;
    }

    public static byte[] concatenateSegmentedMessages(final HashMap<Integer, byte[]> segmentedMessages) {
        final int length = getSegmentedMessageLength(segmentedMessages);
        final ByteBuffer completeBuffer = ByteBuffer.allocate(length);
        completeBuffer.order(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < segmentedMessages.size(); i++) {
            completeBuffer.put(segmentedMessages.get(i));
        }
        return completeBuffer.array();
    }

    /**
     * Returns the opcode within the access payload
     *
     * @param accessPayload payload
     * @param opcodeCount   number of opcodes
     * @return array of opcodes
     */
    public static int getOpCode(final byte[] accessPayload, final int opcodeCount) {
        switch (opcodeCount) {
            case 1:
                return accessPayload[0];
            case 2:
                return ((short) (((accessPayload[0] << 8)) | (byte) ((accessPayload[1]) & 0xFF)));
            case 3:
                return ((byte) (accessPayload[0] & 0xFF) | (byte) ((accessPayload[1] << 8) & 0xFF) | (byte) ((accessPayload[2] << 16) & 0xFF));
        }
        return -1;
    }

    /**
     * Returns the length of the opcode.
     * If the MSB = 0 then the length is 1
     * If the MSB = 1 then the length is 2
     * If the MSB = 2 then the length is 3
     *
     * @param opCode operation code
     * @return length of opcodes
     */
    public static byte[] getOpCodes(final int opCode) {
        if ((opCode & 0xC00000) == 0xC00000) {
            return new byte[]{(byte) ((opCode >> 16) & 0xFF), (byte) ((opCode >> 8) & 0xFF), (byte) (opCode & 0xFF)};
        } else if ((opCode & 0xFF8000) == 0x8000) {
            return new byte[]{(byte) ((opCode >> 8) & 0xFF), (byte) (opCode & 0xFF)};
        } else {
            //return new byte[]{ (byte) ((opCode >> 8) & 0xFF), (byte) (opCode & 0xFF)};
            return new byte[]{(byte) opCode};
        }
    }

    /**
     * Returns the length of the opcode.
     * If the MSB = 0 then the length is 1
     * If the MSB = 1 then the length is 2
     * If the MSB = 2 then the length is 3
     *
     * @param opCode operation code
     * @return length of opcodes
     */
    public static byte[] getOpCodes(final int opCode, final int companyIdentifier) {
        if (companyIdentifier != 0xFFFF) {
            return new byte[]{(byte) ((0b11 << 6) | opCode), (byte) (companyIdentifier & 0x00FF), (byte) ((companyIdentifier >> 8) & 0x00FF)};
        }
        return null;
    }

    /**
     * Checks if the publish ttl value is within the allowed range
     *
     * @param publishTtl publish ttl
     * @return true if valid and false otherwise
     */
    public static boolean validatePublishTtl(final int publishTtl) {
        return (publishTtl < PROHIBITED_PUBLISH_TTL_MIN) || (publishTtl > PROHIBITED_PUBLISH_TTL_MAX);
    }
}