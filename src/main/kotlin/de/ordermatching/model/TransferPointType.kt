package de.ordermatching.model

enum class TransferPointType {

    INTERNAL, //can only be used by the owning parcel service
    LOCKER,
    SHOP
}