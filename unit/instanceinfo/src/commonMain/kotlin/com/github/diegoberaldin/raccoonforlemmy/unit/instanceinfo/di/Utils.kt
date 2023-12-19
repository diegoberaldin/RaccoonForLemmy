package com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.di

import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.InstanceInfoMviModel

expect fun getInstanceInfoViewModel(
    url: String,
): InstanceInfoMviModel
