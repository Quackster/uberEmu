using System;
using System.Data;

using Uber.Storage;
using Uber.HabboHotel.GameClients;
using Uber.Messages;

namespace Uber.HabboHotel.Catalogs
{
    class VoucherHandler
    {
        public VoucherHandler() { }

        public Boolean IsValidCode(string Code)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("code", Code);

                if (dbClient.ReadDataRow("SELECT null FROM credit_vouchers WHERE code = @code LIMIT 1") != null)
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
                dbClient.AddParamWithValue("code", Code);
                Data = dbClient.ReadDataRow("SELECT value FROM credit_vouchers WHERE code = @code LIMIT 1");
            }

            if (Data != null)
            {
                return (int)Data[0];
            }

            return 0;
        }

        public void TryDeleteVoucher(string Code)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("code", Code);
                dbClient.ExecuteQuery("DELETE FROM credit_vouchers WHERE code = @code LIMIT 1");
            }
        }

        public void TryRedeemVoucher(GameClient Session, string Code)
        {
            if (!IsValidCode(Code))
            {
                ServerMessage Error = new ServerMessage(213);
                Error.AppendRawInt32(1);
                Session.SendMessage(Error);
                return;
            }

            int Value = GetVoucherValue(Code);

            TryDeleteVoucher(Code);

            if (Value > 0)
            {
                Session.GetHabbo().Credits += Value;
                Session.GetHabbo().UpdateCreditsBalance(true);
            }

            Session.SendMessage(new ServerMessage(212));
        }
    }
}
