package com.precopia.david.lists.view.reauthentication.phone

import com.precopia.david.lists.R

class SmsReAuthViewModel(private val getStringRes: (Int) -> String) :
        ISmsReAuthContract.ViewModel {

    override var phoneNumber: String = ""

    override var verificationId: String = ""


    override val msgGenericError: String
        get() = getStringRes(R.string.error_msg_generic)

    override val msgTryAgainLater: String
        get() = getStringRes(R.string.msg_try_again)


    override val msgInvalidSms: String
        get() = getStringRes(R.string.msg_invalid_sms_code)

    override val msgSmsSent: String
        get() = getStringRes(R.string.msg_sms_sent)


    override val msgAccountDeletionSucceed: String
        get() = getStringRes(R.string.msg_account_deletion_successful)

    override val msgAccountDeletionFailed: String
        get() = getStringRes(R.string.msg_account_deletion_failed)

    override val msgTooManyRequest: String
        get() = getStringRes(R.string.msg_too_many_request)
}