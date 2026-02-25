import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val cityName: String,
    val temperature: Int,
    val description: String,
    val icon: String
)
