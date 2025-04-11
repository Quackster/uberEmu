using System;
using System.Data;

using Uber.Storage;
using Uber.HabboHotel.GameClients;
using Uber.Messages;

namespace Uber.HabboHotel.Navigators
{
    class VoucherHandler
    {
        public VoucherHandler() { }

        public Boolean IsValidCode(string Code)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                if (dbClient.ReadDataRow("SELECT null FROM credit_vouchers WHERE code = '" + Code + "' LIMIT 1") != null)
                {
                    return true;
                }
            }

            return false;
        }

        public int GetVoucherValue(string Code)
        {
            DataRow Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataRow("SELECT value FROM credit_vouchers WHERE code = '" + Code + "' LIMIT 1");
            }

            if (Data != null)
            {
                return (int)Data[0];
            }

            return 0;
        }

        public void TryRedeemVoucher(GameClient Session, string Code)
        {
            if (!IsValidCode(Code))
            {
                Session.SendMessage(new ServerMessage(213));
            }

            int Value = GetVoucherValue(Code);

            if (Value >= 0)
            {
                Session.GetHabbo().Credits += Value;
                Session.GetHabbo().UpdateCreditsBalance(true);
            }

            Session.SendMessage(new ServerMessage(212));
        }
    }
}
