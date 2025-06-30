package com.pla.pladailyboss.network;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class ClientPayloadHandler {

    public static void handleDataOnNetwork(final Data data, final IPayloadContext context) {
        MoneyPickupOverlay.addPickupMessage("◎ Money x" + data.message(), ChatFormatting.GREEN);
        String currencyName = "sdmcoin";
        ErrorCodeStruct<Boolean> result = CurrencyPlayerData.CLIENT.isCurrencyLocked(currencyName);

        if (result.codes == ErrorCodes.SUCCESS) {
            boolean isLocked = result.value;
            double newBalance = CurrencyPlayerData.CLIENT.getBalance(currencyName) + Double.parseDouble(data.message());
            CurrencyPlayerData.CLIENT.updateCurrency(currencyName, newBalance, isLocked);
        }
    }
}