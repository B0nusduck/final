class listener:
    
    def __init__(self, value):
        
        self._value = value
        
    def set(self, value, func):
        old_value = self._value
        self._value = value
        if(old_value != value):
            try:
                func(value)
            except:
                func()
        
    def get(self, value):
        return self._value
