import com.example.deal.model.Product

data class StartNegotiationRequest(
    val product_id: Int,
    val customer_id: String
)

data class BargainRequest(
    val session_id: String,
    val product_id: Int,
    val message: String
)

data class ApiResponseGeneric<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val error: String?
)

data class StartNegotiationData(
    val session_id: String? = null,
    val conversation_id: Int? = null,
    val product: Product? = null,
    val initial_price: Double? = null,
    val welcome_message: String? = null,
    val timestamp: String? = null
)

data class NegotiationMessageData(
    val session_id: String? = null,
    val conversation_id: Int? = null,
    val turn_number: Int? = null,
    val shopkeeper_message: String? = null,
    val current_price: Double? = null,
    val status: String? = null,
    val patience: Int? = null,
    val strategy: String? = null,
    val customer_personality: String? = null,
    val used_llm: Boolean? = null,
    val timestamp: String? = null
)